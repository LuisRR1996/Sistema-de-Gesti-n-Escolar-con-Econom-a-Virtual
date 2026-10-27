package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.PasswordUtil;
import com.mycompany.escuelavirtual.util.db.DatabaseConnection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Prueba de integración del mapeo y de la verificación PBKDF2 contra los datos
 * iniciales (T03). Se omite sola si no hay SQL Server disponible, para que
 * {@code mvn test} siga funcionando sin base de datos.
 *
 * <p>Solo lee: no toca contadores ni bloqueos de los usuarios de desarrollo.</p>
 */
class UsuarioDAOTest {

    private static final UsuarioDAO DAO = new UsuarioDAO();

    @BeforeAll
    static void requiereBaseDeDatos() {
        assumeTrue(DatabaseConnection.isAvailable(), "Sin conexión a SQL Server: prueba omitida");
    }

    @Test
    @DisplayName("findByCorreo normaliza el correo y mapea todas las columnas")
    void findByCorreo() throws SQLException {
        Usuario admin = DAO.findByCorreo("  ADMIN@Colegio.EDU  ");

        assertNotNull(admin, "el usuario administrador de los datos iniciales debe existir");
        assertEquals(Rol.ADMIN, admin.getRol());
        assertTrue(admin.isEstado());
        assertNull(admin.getIdPadre());
        assertTrue(PasswordUtil.verificar("Admin123*", admin.getPasswordHash()),
                "el hash almacenado debe corresponder a la clave de los datos iniciales");
    }

    @Test
    @DisplayName("Un correo inexistente devuelve null, no una excepción")
    void correoInexistente() throws SQLException {
        assertNull(DAO.findByCorreo("no-existe@colegio.edu"));
    }

    @Test
    @DisplayName("La hora del servidor está disponible para medir el bloqueo")
    void ahoraEnServidor() throws SQLException {
        assertNotNull(DAO.ahoraEnServidor());
    }
}
