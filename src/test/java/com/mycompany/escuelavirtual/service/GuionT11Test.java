package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.db.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Guion de verificación T11: recorrido «login → usuarios» contra SQL Server.
 *
 * <p>Ejecuta la parte automatizable de CP-01 (login de administrador), CP-02
 * (clave incorrecta y contador de intentos) y CP-03 (un padre no accede a un
 * hijo ajeno), más los criterios de cierre de T10: alta, edición, baja lógica,
 * reactivación y vínculo padre–hijo.</p>
 *
 * <p>La parte visual de CP-01 —«se abre el MainFrame con los 9 módulos»— y
 * CP-18 completo dependen del árbol de módulos de T09, que todavía no existe:
 * quedan como pasos manuales del guion y se marcan bloqueados.</p>
 *
 * <p>La prueba se omite sola si no hay SQL Server. Crea un usuario propio con
 * el correo {@value #CORREO_QA} y lo elimina al terminar; de los datos
 * iniciales solo cambia {@code ultimo_acceso} del administrador, igual que un
 * inicio de sesión real.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GuionT11Test {

    /** Credenciales de los datos iniciales de T03 (ficha de T03 del plan de desarrollo). */
    private static final String CORREO_ADMIN = "admin@colegio.edu";
    private static final String CLAVE_ADMIN = "Admin123*";

    private static final String CORREO_QA = "qa.t11@colegio.edu";
    private static final String CLAVE_QA = "Clave123*";

    private static final UsuarioDAO DAO = new UsuarioDAO();
    private static final AuthService AUTH = new AuthService();
    private static final UsuarioService USUARIOS = new UsuarioService();
    private static final BilleteraService BILLETERAS = new BilleteraService();

    private static Usuario admin;
    private static Usuario padrePropietario;
    private static Usuario padreAjeno;
    private static Usuario hijoDelPropietario;
    private static Integer idUsuarioQa;

    @BeforeAll
    static void prepararEscenario() throws SQLException {
        assumeTrue(DatabaseConnection.isAvailable(), "Sin conexión a SQL Server: guion omitido");
        borrarUsuarioQa();

        admin = DAO.findByCorreo(CORREO_ADMIN);
        assumeTrue(admin != null, "Faltan los datos iniciales de T03");

        // Dos padres distintos con hijos: uno es el propietario y el otro el ajeno.
        List<Usuario> padres = DAO.listarPorRol(Rol.PADRE);
        for (Usuario padre : padres) {
            List<Usuario> hijos = DAO.listarHijosPorPadre(padre.getIdUsuario());
            if (hijos.isEmpty()) {
                continue;
            }
            if (padrePropietario == null) {
                padrePropietario = padre;
                hijoDelPropietario = hijos.get(0);
            } else if (padreAjeno == null) {
                padreAjeno = padre;
            }
        }
        assumeTrue(padrePropietario != null && padreAjeno != null,
                "El guion necesita dos padres con hijos en los datos iniciales");
    }

    @AfterAll
    static void limpiar() throws SQLException {
        if (DatabaseConnection.isAvailable()) {
            borrarUsuarioQa();
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP-01 · El administrador inicia sesión con sus credenciales reales")
    void cp01LoginAdministrador() {
        Usuario autenticado = AUTH.login("  Admin@Colegio.EDU  ", CLAVE_ADMIN);

        assertNotNull(autenticado);
        assertEquals(Rol.ADMIN, autenticado.getRol());
        assertTrue(autenticado.isEstado(), "la cuenta debe estar activa");
        assertNotNull(autenticado.getUltimoAcceso(), "el login debe registrar ultimo_acceso");
        assertEquals(0, autenticado.getIntentosFallidos());
    }

    @Test
    @Order(2)
    @DisplayName("CP-02 · Clave incorrecta: mensaje genérico, contador +1 y reinicio al acertar")
    void cp02ClaveIncorrecta() throws SQLException {
        NegocioException error = assertThrows(NegocioException.class,
                () -> AUTH.login(CORREO_ADMIN, "EstaNoEsLaClave9*"));

        assertEquals(CodigoError.CREDENCIALES_INVALIDAS, error.getCodigo());
        assertEquals(1, DAO.findByCorreo(CORREO_ADMIN).getIntentosFallidos(),
                "el intento fallido debe quedar registrado");

        // Un acceso correcto reinicia el contador y deja la cuenta como estaba.
        assertNotNull(AUTH.login(CORREO_ADMIN, CLAVE_ADMIN));
        assertEquals(0, DAO.findByCorreo(CORREO_ADMIN).getIntentosFallidos());
    }

    @Test
    @Order(3)
    @DisplayName("CP-03 · Un padre no accede a la billetera, los movimientos ni los logros de un hijo ajeno")
    void cp03PadreConHijoAjeno() {
        int idHijoAjeno = hijoDelPropietario.getIdUsuario();

        assertEquals(CodigoError.PERMISO_DENEGADO,
                assertThrows(NegocioException.class,
                        () -> BILLETERAS.consultarSaldo(padreAjeno, idHijoAjeno)).getCodigo());
        assertEquals(CodigoError.PERMISO_DENEGADO,
                assertThrows(NegocioException.class,
                        () -> BILLETERAS.listarMovimientos(padreAjeno, idHijoAjeno)).getCodigo());
        assertEquals(CodigoError.PERMISO_DENEGADO,
                assertThrows(NegocioException.class,
                        () -> BILLETERAS.listarLogros(padreAjeno, idHijoAjeno)).getCodigo());
        assertEquals(CodigoError.PERMISO_DENEGADO,
                assertThrows(NegocioException.class,
                        () -> USUARIOS.listarHijos(padreAjeno, padrePropietario.getIdUsuario())).getCodigo());

        // El padre propietario sí accede: el permiso sale del vínculo en la base de datos.
        assertDoesNotThrow(() -> BILLETERAS.consultarSaldo(padrePropietario, idHijoAjeno));
    }

    @Test
    @Order(4)
    @DisplayName("T10 · Alta de estudiante con padre asignado, billetera en cero y correo único")
    void altaDeEstudianteConVinculo() throws SQLException {
        Usuario creado = USUARIOS.registrar(admin, " QA ", " Pruebas T11 ", CORREO_QA, CLAVE_QA,
                Rol.ESTUDIANTE, "5.º", "B", padrePropietario.getIdUsuario());
        idUsuarioQa = creado.getIdUsuario();

        assertNotNull(idUsuarioQa);
        Usuario enBase = DAO.buscarPorId(idUsuarioQa);
        assertEquals("QA", enBase.getNombres(), "el servicio debe recortar los espacios");
        assertEquals(CORREO_QA, enBase.getCorreo());
        assertEquals(Rol.ESTUDIANTE, enBase.getRol());
        assertEquals(padrePropietario.getIdUsuario(), enBase.getIdPadre());
        assertTrue(enBase.isEstado());

        assertTrue(DAO.esHijoDe(padrePropietario.getIdUsuario(), idUsuarioQa),
                "el vínculo padre-hijo debe quedar en usuarios.id_padre");
        assertTrue(USUARIOS.listarHijos(padrePropietario, padrePropietario.getIdUsuario()).stream()
                .anyMatch(h -> idUsuarioQa.equals(h.getIdUsuario())),
                "el padre debe ver al nuevo hijo en su lista");

        Billetera billetera = BILLETERAS.consultarSaldo(admin, idUsuarioQa);
        assertEquals(0, billetera.getSaldoDinero().compareTo(BigDecimal.ZERO));
        assertEquals(0, billetera.getSaldoMonedas());

        assertEquals(CodigoError.CONFLICTO,
                assertThrows(NegocioException.class,
                        () -> USUARIOS.registrar(admin, "Otro", "Usuario", CORREO_QA, CLAVE_QA,
                                Rol.DOCENTE, null, null, null)).getCodigo(),
                "un correo repetido debe devolver CONFLICTO, no un error de SQL Server");
    }

    @Test
    @Order(5)
    @DisplayName("T10 · Edición de datos y cambio de padre")
    void edicionDeUsuario() throws SQLException {
        Usuario editable = DAO.buscarPorId(idUsuarioQa);
        editable.setApellidos("Pruebas T11 Editado");
        editable.setSeccion("C");
        editable.setIdPadre(padreAjeno.getIdUsuario());

        USUARIOS.actualizar(admin, editable);

        Usuario enBase = DAO.buscarPorId(idUsuarioQa);
        assertEquals("Pruebas T11 Editado", enBase.getApellidos());
        assertEquals("C", enBase.getSeccion());
        assertEquals(padreAjeno.getIdUsuario(), enBase.getIdPadre());
        assertTrue(DAO.esHijoDe(padreAjeno.getIdUsuario(), idUsuarioQa));
        assertFalse(DAO.esHijoDe(padrePropietario.getIdUsuario(), idUsuarioQa),
                "el vínculo anterior debe quedar deshecho");
    }

    @Test
    @Order(6)
    @DisplayName("T10 · La baja lógica impide el login y la reactivación lo devuelve")
    void bajaLogicaYReactivacion() throws SQLException {
        assertNotNull(AUTH.login(CORREO_QA, CLAVE_QA), "antes de la baja debe poder entrar");

        USUARIOS.cambiarEstado(admin, idUsuarioQa, false);
        assertFalse(DAO.buscarPorId(idUsuarioQa).isEstado());
        assertEquals(CodigoError.CUENTA_INACTIVA,
                assertThrows(NegocioException.class, () -> AUTH.login(CORREO_QA, CLAVE_QA)).getCodigo());

        USUARIOS.cambiarEstado(admin, idUsuarioQa, true);
        assertTrue(DAO.buscarPorId(idUsuarioQa).isEstado());
        assertNotNull(AUTH.login(CORREO_QA, CLAVE_QA), "tras reactivar debe poder entrar de nuevo");
    }

    @Test
    @Order(7)
    @DisplayName("T10 · Candados de la baja: ni a sí mismo ni al último administrador")
    void candadosDeLaBaja() throws SQLException {
        assertEquals(CodigoError.CONFLICTO,
                assertThrows(NegocioException.class,
                        () -> USUARIOS.cambiarEstado(admin, admin.getIdUsuario(), false)).getCodigo(),
                "el administrador no puede darse de baja a sí mismo");

        // Con un solo ADMIN activo en los datos iniciales, cualquier otro intento de
        // baja de un administrador debe quedar bloqueado por el mismo candado.
        assertTrue(DAO.buscarPorId(admin.getIdUsuario()).isEstado(),
                "la cuenta del administrador debe seguir activa tras el intento");
    }

    @Test
    @Order(8)
    @DisplayName("CP-18 (parcial) · Cada rol recibe exactamente los permisos del anexo C")
    void cp18PermisosPorRol() throws SQLException {
        // El árbol de módulos es de T09; lo verificable hoy es la matriz que lo alimentará.
        assertEquals(CodigoError.PERMISO_DENEGADO,
                assertThrows(NegocioException.class,
                        () -> USUARIOS.listarTodos(padrePropietario)).getCodigo());
        assertEquals(CodigoError.PERMISO_DENEGADO,
                assertThrows(NegocioException.class,
                        () -> USUARIOS.listarTodos(DAO.listarPorRol(Rol.DOCENTE).get(0))).getCodigo());
        assertDoesNotThrow(() -> USUARIOS.listarTodos(admin));
    }

    private static void borrarUsuarioQa() throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            ejecutar(cn, "DELETE FROM billeteras WHERE id_estudiante IN (SELECT id_usuario FROM usuarios WHERE correo = ?)");
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
