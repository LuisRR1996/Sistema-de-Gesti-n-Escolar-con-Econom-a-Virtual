package com.mycompany.escuelavirtual.util;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.Permisos.Accion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba que {@link Permisos} dice lo mismo que el anexo C del plan.
 *
 * <p>Cubre la parte verificable sin base de datos de CP-03 (un padre no accede
 * a un hijo ajeno) y CP-10 (un estudiante no se asigna monedas).</p>
 */
class PermisosTest {

    private static Usuario usuario(int id, Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombres("Nombre");
        u.setApellidos("Apellido");
        u.setRol(rol);
        u.setEstado(true);
        return u;
    }

    @Test
    @DisplayName("Solo DOCENTE y ADMIN registran logros (RN-09, CP-10)")
    void registrarLogro() {
        assertTrue(Permisos.puede(usuario(2, Rol.DOCENTE), Accion.REGISTRAR_LOGRO));
        assertTrue(Permisos.puede(usuario(1, Rol.ADMIN), Accion.REGISTRAR_LOGRO));
        assertFalse(Permisos.puede(usuario(5, Rol.ESTUDIANTE), Accion.REGISTRAR_LOGRO));
        assertFalse(Permisos.puede(usuario(3, Rol.PADRE), Accion.REGISTRAR_LOGRO));

        NegocioException e = assertThrows(NegocioException.class,
                () -> Permisos.exigir(usuario(5, Rol.ESTUDIANTE), Accion.REGISTRAR_LOGRO));
        assertEquals(CodigoError.PERMISO_DENEGADO, e.getCodigo());
    }

    @Test
    @DisplayName("Solo el ADMIN aprueba recargas y administra usuarios y catálogo")
    void accionesDeAdministracion() {
        for (Accion accion : new Accion[]{Accion.RESOLVER_RECARGA, Accion.ADMINISTRAR_USUARIOS,
            Accion.ADMINISTRAR_CATALOGO, Accion.VER_CUADRE_BILLETERAS}) {
            assertTrue(Permisos.puede(usuario(1, Rol.ADMIN), accion), accion.name());
            assertFalse(Permisos.puede(usuario(3, Rol.PADRE), accion), accion.name());
            assertFalse(Permisos.puede(usuario(2, Rol.DOCENTE), accion), accion.name());
            assertFalse(Permisos.puede(usuario(5, Rol.ESTUDIANTE), accion), accion.name());
        }
    }

    @Test
    @DisplayName("Comprar y canjear es exclusivo del estudiante")
    void comprarYCanjear() {
        assertTrue(Permisos.puede(usuario(5, Rol.ESTUDIANTE), Accion.COMPRAR_CON_DINERO));
        assertTrue(Permisos.puede(usuario(5, Rol.ESTUDIANTE), Accion.CANJEAR_CON_MONEDAS));
        assertFalse(Permisos.puede(usuario(3, Rol.PADRE), Accion.COMPRAR_CON_DINERO));
        assertFalse(Permisos.puede(usuario(1, Rol.ADMIN), Accion.CANJEAR_CON_MONEDAS));
    }

    @Test
    @DisplayName("El padre solo accede a sus propios hijos (RN-08, CP-03)")
    void accesoDelPadre() {
        Usuario padre = usuario(3, Rol.PADRE);

        assertDoesNotThrow(() -> Permisos.exigirAccesoAEstudiante(padre, 5, true));

        NegocioException e = assertThrows(NegocioException.class,
                () -> Permisos.exigirAccesoAEstudiante(padre, 6, false));
        assertEquals(CodigoError.PERMISO_DENEGADO, e.getCodigo());
    }

    @Test
    @DisplayName("El estudiante solo accede a su propia billetera")
    void accesoDelEstudiante() {
        Usuario estudiante = usuario(5, Rol.ESTUDIANTE);

        assertDoesNotThrow(() -> Permisos.exigirAccesoAEstudiante(estudiante, 5, false));
        assertThrows(NegocioException.class,
                () -> Permisos.exigirAccesoAEstudiante(estudiante, 6, false));
    }

    @Test
    @DisplayName("El ADMIN accede a cualquier estudiante y el DOCENTE a ninguno")
    void accesoDeAdminYDocente() {
        assertDoesNotThrow(() -> Permisos.exigirAccesoAEstudiante(usuario(1, Rol.ADMIN), 7, false));
        assertThrows(NegocioException.class,
                () -> Permisos.exigirAccesoAEstudiante(usuario(2, Rol.DOCENTE), 7, false));
    }

    @Test
    @DisplayName("Sin sesión no hay permiso")
    void sinSesion() {
        assertFalse(Permisos.puede(null, Accion.VER_BILLETERA_PROPIA));
        assertThrows(NegocioException.class, () -> Permisos.exigir(null, Accion.VER_BILLETERA_PROPIA));
        assertThrows(NegocioException.class, () -> Permisos.exigirAccesoAEstudiante(null, 5, true));
        assertThrows(NegocioException.class,
                () -> Permisos.exigirAccesoAEstudiante(usuario(1, Rol.ADMIN), 0, true));
    }
}
