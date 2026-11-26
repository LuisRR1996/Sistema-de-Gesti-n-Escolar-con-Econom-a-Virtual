package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.CuadreBilletera;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.Modulo;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * T12 · Pruebas de integración de los criterios de salida de la Etapa 1.
 *
 * <p>Cubre contra SQL Server lo que el guion de T11 dejó pendiente o solo
 * comprobado con dobles en memoria: el cierre de CP-01 (el administrador entra
 * y ve los 9 módulos del anexo C), CP-16 (bloqueo tras
 * {@link AuthService#MAX_INTENTOS} intentos), CP-17 (una cuenta inactiva no
 * entra), CP-18 (cada rol real de la base recibe sus módulos) y el criterio
 * transversal de que ninguna billetera queda descuadrada ni en negativo.</p>
 *
 * <p>CP-02, CP-03 y los criterios de T10 se verifican en {@code GuionT11Test}
 * y no se repiten aquí.</p>
 *
 * <p>La prueba se omite sola si no hay SQL Server. El bloqueo y la baja lógica
 * se ensayan sobre un usuario propio con el correo {@value #CORREO_QA}, que la
 * clase crea y elimina, para no dejar bloqueada ninguna cuenta de los datos
 * iniciales.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PruebasEtapa1Test {

    private static final String CORREO_ADMIN = "admin@colegio.edu";
    private static final String CLAVE_ADMIN = "Admin123*";

    private static final String CORREO_QA = "qa.t12@colegio.edu";
    private static final String CLAVE_QA = "Clave123*";
    private static final String CLAVE_EQUIVOCADA = "EstaNoEsLaClave9*";

    private static final UsuarioDAO DAO = new UsuarioDAO();
    private static final AuthService AUTH = new AuthService();
    private static final UsuarioService USUARIOS = new UsuarioService();
    private static final ReporteService REPORTES = new ReporteService();

    private static Usuario admin;
    private static Integer idUsuarioQa;

    @BeforeAll
    static void prepararEscenario() throws SQLException {
        assumeTrue(DatabaseConnection.isAvailable(), "Sin conexión a SQL Server: pruebas omitidas");
        borrarUsuarioQa();

        admin = DAO.findByCorreo(CORREO_ADMIN);
        assumeTrue(admin != null, "Faltan los datos iniciales de T03");

        idUsuarioQa = USUARIOS.registrar(admin, "QA", "Pruebas T12", CORREO_QA, CLAVE_QA,
                Rol.DOCENTE, null, null, null).getIdUsuario();
    }

    @AfterAll
    static void limpiar() throws SQLException {
        if (DatabaseConnection.isAvailable()) {
            borrarUsuarioQa();
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP-01 · El administrador inicia sesión y su árbol tiene los 9 módulos")
    void cp01LoginYNueveModulos() {
        Usuario autenticado = AUTH.login(CORREO_ADMIN, CLAVE_ADMIN);

        assertEquals(Rol.ADMIN, autenticado.getRol());
        assertNotNull(autenticado.getUltimoAcceso(), "el login debe registrar ultimo_acceso");

        List<Modulo> arbol = Modulo.visiblesPara(autenticado.getRol());
        assertEquals(9, arbol.size(), "el administrador ve M01 a M09 (anexo C y D-06)");
        assertEquals(List.of(Modulo.values()), arbol);
    }

    @Test
    @Order(2)
    @DisplayName("CP-16 · Al tercer intento fallido la cuenta queda bloqueada 15 minutos")
    void cp16BloqueoTrasTresIntentos() throws SQLException {
        for (int intento = 1; intento < AuthService.MAX_INTENTOS; intento++) {
            assertEquals(CodigoError.CREDENCIALES_INVALIDAS,
                    assertThrows(NegocioException.class,
                            () -> AUTH.login(CORREO_QA, CLAVE_EQUIVOCADA)).getCodigo());
            assertEquals(intento, DAO.findByCorreo(CORREO_QA).getIntentosFallidos());
        }

        assertEquals(CodigoError.CUENTA_BLOQUEADA,
                assertThrows(NegocioException.class,
                        () -> AUTH.login(CORREO_QA, CLAVE_EQUIVOCADA)).getCodigo(),
                "el tercer fallo debe bloquear la cuenta");

        Usuario bloqueado = DAO.findByCorreo(CORREO_QA);
        LocalDateTime ahora = DAO.ahoraEnServidor();
        assertTrue(bloqueado.estaBloqueado(ahora), "bloqueado_hasta debe quedar en el futuro");
        long minutos = Duration.between(ahora, bloqueado.getBloqueadoHasta()).toMinutes();
        assertTrue(minutos >= AuthService.MINUTOS_BLOQUEO - 1 && minutos <= AuthService.MINUTOS_BLOQUEO,
                "el bloqueo debe durar " + AuthService.MINUTOS_BLOQUEO + " minutos, no " + minutos);

        // Con la cuenta bloqueada, ni la clave correcta entra hasta que expire.
        assertEquals(CodigoError.CUENTA_BLOQUEADA,
                assertThrows(NegocioException.class, () -> AUTH.login(CORREO_QA, CLAVE_QA)).getCodigo());
    }

    @Test
    @Order(3)
    @DisplayName("CP-16 · Expirado el bloqueo la clave correcta vuelve a entrar y el contador se reinicia")
    void cp16ExpiracionDelBloqueo() throws SQLException {
        // No se espera un cuarto de hora: se adelanta bloqueado_hasta, que es lo que lee el servicio.
        vencerBloqueo();

        assertNotNull(AUTH.login(CORREO_QA, CLAVE_QA), "pasado el bloqueo debe poder entrar");
        Usuario trasEntrar = DAO.findByCorreo(CORREO_QA);
        assertEquals(0, trasEntrar.getIntentosFallidos());
        assertTrue(trasEntrar.getBloqueadoHasta() == null
                || !trasEntrar.estaBloqueado(DAO.ahoraEnServidor()),
                "el acceso correcto debe levantar el bloqueo");
    }

    @Test
    @Order(4)
    @DisplayName("CP-17 · Una cuenta dada de baja no inicia sesión aunque la clave sea correcta")
    void cp17CuentaInactiva() throws SQLException {
        USUARIOS.cambiarEstado(admin, idUsuarioQa, false);

        assertEquals(CodigoError.CUENTA_INACTIVA,
                assertThrows(NegocioException.class, () -> AUTH.login(CORREO_QA, CLAVE_QA)).getCodigo());
        assertEquals(0, DAO.findByCorreo(CORREO_QA).getIntentosFallidos(),
                "una cuenta inactiva no acumula intentos fallidos");

        USUARIOS.cambiarEstado(admin, idUsuarioQa, true);
        assertNotNull(AUTH.login(CORREO_QA, CLAVE_QA));
    }

    @Test
    @Order(5)
    @DisplayName("CP-18 · Cada usuario real de la base recibe los módulos de su rol")
    void cp18ArbolPorRolConUsuariosReales() throws SQLException {
        for (Rol rol : Rol.values()) {
            List<Usuario> delRol = DAO.listarPorRol(rol);
            assumeTrue(!delRol.isEmpty(), "Faltan usuarios con rol " + rol);
            for (Usuario usuario : delRol) {
                assertEquals(Modulo.visiblesPara(rol), Modulo.visiblesPara(usuario.getRol()),
                        "el árbol depende solo del rol, no del usuario");
            }
            assertTrue(Modulo.visiblesPara(rol).contains(Modulo.M01),
                    "todo rol autenticado ve M01");
        }
        assertEquals(9, Modulo.visiblesPara(Rol.ADMIN).size());
        assertEquals(3, Modulo.visiblesPara(Rol.DOCENTE).size());
    }

    @Test
    @Order(6)
    @DisplayName("Criterio transversal · Ninguna billetera descuadrada ni en negativo")
    void criterioTransversalDeSaldos() {
        List<CuadreBilletera> cuadre = REPORTES.cuadreBilleteras(admin);

        assertTrue(!cuadre.isEmpty(), "la vista vw_cuadre_billeteras debe devolver filas");
        assertEquals(List.of(), REPORTES.descuadres(admin),
                "vw_cuadre_billeteras no debe tener diferencias");
        for (CuadreBilletera fila : cuadre) {
            assertTrue(fila.saldoDinero().signum() >= 0,
                    "saldo en dinero negativo en la billetera " + fila.idBilletera());
            assertTrue(fila.saldoMonedas() >= 0,
                    "saldo en monedas negativo en la billetera " + fila.idBilletera());
        }
    }

    /** Adelanta el fin del bloqueo para no esperar los 15 minutos reales. */
    private static void vencerBloqueo() throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE usuarios SET bloqueado_hasta = DATEADD(MINUTE, -1, SYSDATETIME())"
                        + " WHERE correo = ?")) {
            ps.setString(1, CORREO_QA);
            ps.executeUpdate();
        }
    }

    private static void borrarUsuarioQa() throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            ejecutar(cn, "DELETE FROM billeteras WHERE id_estudiante IN"
                    + " (SELECT id_usuario FROM usuarios WHERE correo = ?)");
            ejecutar(cn, "DELETE FROM usuarios WHERE correo = ?");
        }
    }

    private static void ejecutar(Connection cn, String sql) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, CORREO_QA);
            ps.executeUpdate();
        }
    }
}
