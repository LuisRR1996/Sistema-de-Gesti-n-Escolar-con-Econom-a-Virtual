package com.mycompany.escuelavirtual.util;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Control de acceso por rol (ADR-03 e I-11 del plan de desarrollo).
 *
 * <p>La matriz de este enum es la traducción literal del anexo C del plan, que
 * es la única fuente de permisos para el árbol de navegación, para los
 * servicios y para las pruebas CP-03, CP-10 y CP-18. Cualquier cambio se hace
 * primero en el anexo y después aquí.</p>
 *
 * <p>Los servicios invocan estas comprobaciones antes de tocar la base de
 * datos, de modo que ninguna ruta pueda esquivarlas desde la interfaz.</p>
 */
public final class Permisos {

    /** Acciones del anexo C que el código necesita autorizar. */
    public enum Accion {
        VER_BILLETERA_PROPIA(Rol.ESTUDIANTE),
        VER_BILLETERA_ESTUDIANTE(Rol.PADRE, Rol.ADMIN),
        REGISTRAR_RECARGA(Rol.PADRE, Rol.ADMIN),
        RESOLVER_RECARGA(Rol.ADMIN),
        COMPRAR_CON_DINERO(Rol.ESTUDIANTE),
        CANJEAR_CON_MONEDAS(Rol.ESTUDIANTE),
        VER_LOGROS_PROPIOS(Rol.ESTUDIANTE),
        VER_LOGROS_ESTUDIANTE(Rol.PADRE, Rol.ADMIN),
        VER_LOGROS_OTORGADOS(Rol.DOCENTE, Rol.ADMIN),
        REGISTRAR_LOGRO(Rol.DOCENTE, Rol.ADMIN),
        LISTAR_ESTUDIANTES(Rol.DOCENTE, Rol.ADMIN),
        ADMINISTRAR_USUARIOS(Rol.ADMIN),
        ADMINISTRAR_CATALOGO(Rol.ADMIN),
        VER_HISTORIAL_ESTUDIANTE(Rol.PADRE, Rol.ADMIN),
        GENERAR_REPORTES(Rol.PADRE, Rol.ADMIN),
        VER_CUADRE_BILLETERAS(Rol.ADMIN);

        private final Set<Rol> roles;

        Accion(Rol... roles) {
            this.roles = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(roles)));
        }

        public Set<Rol> getRoles() {
            return roles;
        }
    }

    private Permisos() {
        // Clase de utilidad.
    }

    /** {@code true} si el rol del actor aparece en la fila del anexo C. */
    public static boolean puede(Usuario actor, Accion accion) {
        return actor != null && actor.getRol() != null && accion.getRoles().contains(actor.getRol());
    }

    /**
     * @throws NegocioException con {@link CodigoError#PERMISO_DENEGADO} si el
     *                          actor no tiene la acción asignada en el anexo C
     */
    public static void exigir(Usuario actor, Accion accion) {
        if (!puede(actor, accion)) {
            throw new NegocioException(CodigoError.PERMISO_DENEGADO);
        }
    }

    /**
     * Autoriza la consulta de los datos de un estudiante concreto (RN-08).
     *
     * <p>El administrador ve a todos; el estudiante solo se ve a sí mismo; el
     * padre solo ve a sus hijos, y por eso quien llama debe resolver el vínculo
     * contra {@code usuarios.id_padre} y pasarlo en {@code esSuHijo}. Los
     * docentes no consultan billeteras ni historiales de estudiantes.</p>
     */
    public static void exigirAccesoAEstudiante(Usuario actor, Integer idEstudiante, boolean esSuHijo) {
        if (actor == null || actor.getRol() == null || !Validador.esIdValido(idEstudiante)) {
            throw new NegocioException(CodigoError.PERMISO_DENEGADO);
        }
        boolean autorizado = switch (actor.getRol()) {
            case ADMIN -> true;
            case ESTUDIANTE -> idEstudiante.equals(actor.getIdUsuario());
            case PADRE -> esSuHijo;
            case DOCENTE -> false;
        };
        if (!autorizado) {
            throw new NegocioException(CodigoError.PERMISO_DENEGADO);
        }
    }
}
