package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.PasswordUtil;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CP-02 (clave incorrecta), CP-16 (bloqueo tras 3 intentos) y CP-17 (usuario
 * inactivo) sobre la lógica de {@link AuthService}.
 *
 * <p>Usa un {@link UsuarioDAO} de prueba en memoria que reproduce el
 * comportamiento de las sentencias SQL: así el bloqueo se verifica siempre,
 * aunque no haya SQL Server disponible.</p>
 */
class AuthServiceTest {

    private static final String CLAVE = "Admin123*";

    private DaoEnMemoria dao;
    private AuthService auth;

    @BeforeEach
    void prepararUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombres("Ana");
        usuario.setApellidos("Pérez");
        usuario.setCorreo("admin@colegio.edu");
        usuario.setPasswordHash(PasswordUtil.hash(CLAVE));
        usuario.setRol(Rol.ADMIN);
        usuario.setEstado(true);

        dao = new DaoEnMemoria(usuario);
        auth = new AuthService(dao);
    }

    @Test
    @DisplayName("Credenciales correctas: devuelve el usuario y reinicia el contador")
    void loginCorrecto() {
        dao.usuario.setIntentosFallidos(2);

        Usuario autenticado = auth.login("  Admin@Colegio.edu ", CLAVE);

        assertNotNull(autenticado);
        assertEquals(Rol.ADMIN, autenticado.getRol());
        assertEquals(0, dao.usuario.getIntentosFallidos());
        assertNotNull(dao.ultimoAcceso, "debe registrar ultimo_acceso");
    }

    @Test
    @DisplayName("CP-02: clave incorrecta devuelve credenciales inválidas y suma un intento")
    void claveIncorrecta() {
        NegocioException error = assertThrows(NegocioException.class,
                () -> auth.login("admin@colegio.edu", "Clave_Mala1*"));

        assertEquals(CodigoError.CREDENCIALES_INVALIDAS, error.getCodigo());
        assertEquals(1, dao.usuario.getIntentosFallidos());
    }

    @Test
    @DisplayName("CP-02: correo inexistente no revela si la cuenta existe")
    void correoInexistente() {
        NegocioException error = assertThrows(NegocioException.class,
                () -> auth.login("nadie@colegio.edu", CLAVE));

        assertEquals(CodigoError.CREDENCIALES_INVALIDAS, error.getCodigo());
    }

    @Test
    @DisplayName("CP-16: al tercer intento fallido la cuenta queda bloqueada 15 minutos")
    void bloqueoTrasTresIntentos() {
        for (int i = 0; i < AuthService.MAX_INTENTOS - 1; i++) {
            assertThrows(NegocioException.class, () -> auth.login("admin@colegio.edu", "Clave_Mala1*"));
        }

        NegocioException tercero = assertThrows(NegocioException.class,
                () -> auth.login("admin@colegio.edu", "Clave_Mala1*"));
        assertEquals(CodigoError.CUENTA_BLOQUEADA, tercero.getCodigo());

        // La clave correcta tampoco entra mientras dure el bloqueo.
        NegocioException conClaveBuena = assertThrows(NegocioException.class,
                () -> auth.login("admin@colegio.edu", CLAVE));
        assertEquals(CodigoError.CUENTA_BLOQUEADA, conClaveBuena.getCodigo());

        long minutos = java.time.Duration.between(dao.ahora, dao.usuario.getBloqueadoHasta()).toMinutes();
        assertEquals(AuthService.MINUTOS_BLOQUEO, minutos);
    }

    @Test
    @DisplayName("CP-16: pasado el bloqueo la clave correcta vuelve a entrar")
    void bloqueoExpira() {
        for (int i = 0; i < AuthService.MAX_INTENTOS; i++) {
            assertThrows(NegocioException.class, () -> auth.login("admin@colegio.edu", "Clave_Mala1*"));
        }

        dao.ahora = dao.ahora.plusMinutes(AuthService.MINUTOS_BLOQUEO + 1);

        assertNotNull(auth.login("admin@colegio.edu", CLAVE));
    }

    @Test
    @DisplayName("CP-17: un usuario inactivo no puede iniciar sesión")
    void usuarioInactivo() {
        dao.usuario.setEstado(false);

        NegocioException error = assertThrows(NegocioException.class,
                () -> auth.login("admin@colegio.edu", CLAVE));

        assertEquals(CodigoError.CUENTA_INACTIVA, error.getCodigo());
    }

    @Test
    @DisplayName("Un correo mal escrito no llega a consultar la base")
    void correoInvalido() {
        NegocioException error = assertThrows(NegocioException.class, () -> auth.login("admin", CLAVE));

        assertEquals(CodigoError.CREDENCIALES_INVALIDAS, error.getCodigo());
        assertEquals(0, dao.consultas);
    }

    /** Reproduce en memoria lo que hacen las sentencias de {@link UsuarioDAO}. */
    private static final class DaoEnMemoria extends UsuarioDAO {

        private final Usuario usuario;
        private LocalDateTime ahora = LocalDateTime.of(2026, 10, 28, 9, 0);
        private LocalDateTime ultimoAcceso;
        private int consultas;

        private DaoEnMemoria(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public Usuario findByCorreo(String correo) {
            consultas++;
            return usuario.getCorreo().equalsIgnoreCase(correo) ? usuario : null;
        }

        @Override
        public LocalDateTime ahoraEnServidor() {
            return ahora;
        }

        @Override
        public boolean registrarIntentoFallido(int idUsuario, int maxIntentos, int minutosBloqueo) {
            if (usuario.getIntentosFallidos() + 1 >= maxIntentos) {
                usuario.setIntentosFallidos(0);
                usuario.setBloqueadoHasta(ahora.plusMinutes(minutosBloqueo));
                return true;
            }
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            return false;
        }

        @Override
        public void registrarAccesoExitoso(int idUsuario) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            ultimoAcceso = ahora;
        }
    }
}
