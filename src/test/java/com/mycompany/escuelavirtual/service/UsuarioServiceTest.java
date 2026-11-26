package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.PasswordUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas de M02 que no dependen de SQL Server: permisos de administración
 * (anexo C) y los dos candados de la baja lógica —nadie se da de baja a sí
 * mismo y siempre queda un administrador activo—.
 */
class UsuarioServiceTest {

    private DaoEspia dao;
    private UsuarioService servicio;

    private static Usuario usuario(int id, Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombres("Nombre");
        u.setApellidos("Apellido");
        u.setCorreo("u" + id + "@colegio.edu");
        u.setRol(rol);
        u.setEstado(true);
        return u;
    }

    @BeforeEach
    void prepararServicio() {
        dao = new DaoEspia();
        servicio = new UsuarioService(dao);
    }

    @Test
    @DisplayName("Un docente no puede administrar usuarios")
    void docenteNoAdministra() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> servicio.listarTodos(usuario(2, Rol.DOCENTE)));

        assertEquals(CodigoError.PERMISO_DENEGADO, e.getCodigo());
        assertNull(dao.ultimoCambioEstado, "no debe llegar a la base de datos");
    }

    @Test
    @DisplayName("Un padre no puede pedir los hijos de otro padre (RN-08)")
    void padreNoVeHijosAjenos() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> servicio.listarHijos(usuario(3, Rol.PADRE), 4));

        assertEquals(CodigoError.PERMISO_DENEGADO, e.getCodigo());
    }

    @Test
    @DisplayName("El administrador no puede darse de baja a sí mismo")
    void adminNoSeDaDeBaja() {
        Usuario admin = usuario(1, Rol.ADMIN);
        dao.usuarios.add(admin);

        NegocioException e = assertThrows(NegocioException.class,
                () -> servicio.cambiarEstado(admin, 1, false));

        assertEquals(CodigoError.CONFLICTO, e.getCodigo());
        assertNull(dao.ultimoCambioEstado);
    }

    @Test
    @DisplayName("No se puede dejar el sistema sin ningún administrador activo")
    void siempreQuedaUnAdmin() {
        Usuario admin = usuario(1, Rol.ADMIN);
        Usuario otroAdmin = usuario(9, Rol.ADMIN);
        dao.usuarios.add(admin);
        dao.usuarios.add(otroAdmin);
        dao.adminsActivosRestantes = 0; // El otro admin sería el último activo.

        NegocioException e = assertThrows(NegocioException.class,
                () -> servicio.cambiarEstado(admin, 9, false));

        assertEquals(CodigoError.CONFLICTO, e.getCodigo());
        assertNull(dao.ultimoCambioEstado);
    }

    @Test
    @DisplayName("Dar de baja a otro usuario sí llega a la base de datos")
    void bajaLogicaNormal() {
        Usuario admin = usuario(1, Rol.ADMIN);
        dao.usuarios.add(admin);
        dao.usuarios.add(usuario(5, Rol.ESTUDIANTE));

        servicio.cambiarEstado(admin, 5, false);

        assertEquals("5=false", dao.ultimoCambioEstado);
    }

    @Test
    @DisplayName("Reactivar no exige comprobar administradores")
    void reactivar() {
        servicio.cambiarEstado(usuario(1, Rol.ADMIN), 5, true);

        assertEquals("5=true", dao.ultimoCambioEstado);
    }

    @Test
    @DisplayName("Un correo duplicado se traduce a CONFLICTO, no a un error de SQL")
    void correoDuplicado() {
        dao.errorAlInsertar = new SQLException("Violation of UNIQUE KEY constraint", "23000", 2627);

        NegocioException e = assertThrows(NegocioException.class,
                () -> servicio.registrar(usuario(1, Rol.ADMIN), "Ana", "Pérez",
                        "ana@colegio.edu", "Clave123*", Rol.DOCENTE, null, null, null));

        assertEquals(CodigoError.CONFLICTO, e.getCodigo());
    }

    @Test
    @DisplayName("El alta rechaza un correo con formato inválido antes de tocar la base de datos")
    void correoInvalido() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> servicio.registrar(usuario(1, Rol.ADMIN), "Ana", "Pérez",
                        "ana(arroba)colegio", "Clave123*", Rol.DOCENTE, null, null, null));

        assertEquals(CodigoError.DATO_INVALIDO, e.getCodigo());
        assertNull(dao.insertado);
    }

    @Test
    @DisplayName("El alta de un estudiante guarda la clave como hash, nunca en claro (RN-12)")
    void claveSiempreHasheada() {
        servicio.registrar(usuario(1, Rol.ADMIN), " Luis ", " Ríos ", " Luis@Colegio.EDU ",
                "Clave123*", Rol.ESTUDIANTE, "5.º", "A", 3);

        assertEquals("luis@colegio.edu", dao.insertado.getCorreo());
        assertEquals("Luis", dao.insertado.getNombres());
        assertNotEquals("Clave123*", dao.insertado.getPasswordHash());
        assertTrue(PasswordUtil.verificar("Clave123*", dao.insertado.getPasswordHash()),
                "el hash guardado debe validar la clave original");
        assertEquals(3, dao.insertado.getIdPadre());
    }

    /** DAO de prueba: registra lo que recibe y no abre ninguna conexión. */
    private static final class DaoEspia extends UsuarioDAO {

        private final List<Usuario> usuarios = new ArrayList<>();
        private int adminsActivosRestantes = 1;
        private String ultimoCambioEstado;
        private Usuario insertado;
        private SQLException errorAlInsertar;

        @Override
        public Usuario buscarPorId(int idUsuario) {
            return usuarios.stream()
                    .filter(u -> Integer.valueOf(idUsuario).equals(u.getIdUsuario()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int contarAdminsActivosExcepto(int idUsuario) {
            return adminsActivosRestantes;
        }

        @Override
        public void cambiarEstado(int idUsuario, boolean activo) {
            ultimoCambioEstado = idUsuario + "=" + activo;
        }

        @Override
        public Usuario insertar(Usuario u) throws SQLException {
            if (errorAlInsertar != null) {
                throw errorAlInsertar;
            }
            insertado = u;
            u.setIdUsuario(100);
            return u;
        }

        @Override
        public List<Usuario> listarTodos() {
            return usuarios;
        }

        @Override
        public List<Usuario> listarHijosPorPadre(int idPadre) {
            return List.of();
        }
    }
}
