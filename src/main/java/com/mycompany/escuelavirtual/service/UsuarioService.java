package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.PasswordUtil;
import com.mycompany.escuelavirtual.util.Permisos;
import com.mycompany.escuelavirtual.util.Permisos.Accion;
import com.mycompany.escuelavirtual.util.Validador;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Gestión de usuarios (M02): alta, edición, baja lógica y vínculo padre–hijo.
 *
 * <p>Todas las operaciones reciben como primer parámetro el usuario de la
 * sesión (ADR-03) y comprueban el permiso con {@link Permisos} antes de tocar
 * la base de datos.</p>
 */
public class UsuarioService extends ServicioBase {

    /** Códigos de SQL Server para violación de clave única o duplicada. */
    private static final int ERROR_CLAVE_DUPLICADA = 2627;
    private static final int ERROR_INDICE_DUPLICADO = 2601;

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this(new UsuarioDAO());
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public List<Usuario> listarTodos(Usuario actor) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_USUARIOS);
        return consultar(() -> usuarioDAO.listarTodos());
    }

    /** Lista de estudiantes o padres activos; la usan el docente y el administrador. */
    public List<Usuario> listarPorRol(Usuario actor, Rol rol) {
        Permisos.exigir(actor, rol == Rol.ESTUDIANTE
                ? Accion.LISTAR_ESTUDIANTES
                : Accion.ADMINISTRAR_USUARIOS);
        return consultar(() -> usuarioDAO.listarPorRol(rol));
    }

    /**
     * Hijos de un padre. Un padre solo puede pedir los suyos (RN-08); el
     * administrador puede consultar los de cualquiera.
     */
    public List<Usuario> listarHijos(Usuario actor, int idPadre) {
        Permisos.exigir(actor, Accion.VER_BILLETERA_ESTUDIANTE);
        if (actor.getRol() == Rol.PADRE && !Integer.valueOf(idPadre).equals(actor.getIdUsuario())) {
            throw new NegocioException(CodigoError.PERMISO_DENEGADO);
        }
        return consultar(() -> usuarioDAO.listarHijosPorPadre(idPadre));
    }

    public Usuario registrar(Usuario actor, String nombres, String apellidos, String correo,
            String clavePlana, Rol rol, String grado, String seccion, Integer idPadre) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_USUARIOS);
        validarDatos(nombres, apellidos, correo, rol);
        if (clavePlana == null || clavePlana.isBlank()) {
            throw new NegocioException(CodigoError.DATO_INVALIDO, "Ingrese una contraseña inicial.");
        }

        Usuario u = new Usuario();
        u.setNombres(nombres.trim());
        u.setApellidos(apellidos.trim());
        u.setCorreo(Validador.normalizarCorreo(correo));

        // --- NUEVA VALIDACIÓN: Verificar que el correo no exista en la BD ---
        Usuario existente = consultar(() -> usuarioDAO.findByCorreo(u.getCorreo()));
        if (existente != null) {
            throw new NegocioException(CodigoError.CONFLICTO, "Ya existe un usuario registrado con este correo.");
        }
        // --------------------------------------------------------------------

        u.setPasswordHash(PasswordUtil.hash(clavePlana)); // RN-12: nunca se guarda en claro.
        u.setRol(rol);
        u.setGrado(rol == Rol.ESTUDIANTE ? grado : null);
        u.setSeccion(rol == Rol.ESTUDIANTE ? seccion : null);
        u.setIdPadre(rol == Rol.ESTUDIANTE ? idPadre : null);
        u.setEstado(true);
        return consultar(() -> usuarioDAO.insertar(u));
    }

    public void actualizar(Usuario actor, Usuario u) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_USUARIOS);
        if (u == null || !Validador.esIdValido(u.getIdUsuario())) {
            throw new NegocioException(CodigoError.DATO_INVALIDO);
        }
        validarDatos(u.getNombres(), u.getApellidos(), u.getCorreo(), u.getRol());
        if (u.getIdUsuario().equals(actor.getIdUsuario()) && u.getRol() != Rol.ADMIN) {
            throw new NegocioException(CodigoError.CONFLICTO,
                    "No puede quitarse a sí mismo el rol de administrador.");
        }

        u.setCorreo(Validador.normalizarCorreo(u.getCorreo()));

        // --- NUEVA VALIDACIÓN: Verificar que el correo no lo use otro usuario ---
        Usuario existente = consultar(() -> usuarioDAO.findByCorreo(u.getCorreo()));
        if (existente != null && !existente.getIdUsuario().equals(u.getIdUsuario())) {
            throw new NegocioException(CodigoError.CONFLICTO, "El correo ingresado ya está en uso por otro usuario.");
        }
        // ------------------------------------------------------------------------

        ejecutar(() -> usuarioDAO.actualizar(u));
    }

    /**
     * Baja lógica y reactivación.
     *
     * <p>Un administrador no puede darse de baja a sí mismo ni dejar el sistema
     * sin ningún administrador activo, porque nadie podría volver a
     * habilitarlo.</p>
     */
    public void cambiarEstado(Usuario actor, int idUsuario, boolean activo) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_USUARIOS);
        if (!Validador.esIdValido(idUsuario)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO);
        }
        if (!activo) {
            if (Integer.valueOf(idUsuario).equals(actor.getIdUsuario())) {
                throw new NegocioException(CodigoError.CONFLICTO,
                        "No puede darse de baja a sí mismo.");
            }
            Usuario objetivo = consultar(() -> usuarioDAO.buscarPorId(idUsuario));
            if (objetivo == null) {
                throw new NegocioException(CodigoError.NO_ENCONTRADO);
            }
            if (objetivo.getRol() == Rol.ADMIN
                    && consultar(() -> usuarioDAO.contarAdminsActivosExcepto(idUsuario)) == 0) {
                throw new NegocioException(CodigoError.CONFLICTO,
                        "Debe quedar al menos un administrador activo en el sistema.");
            }
        }
        ejecutar(() -> usuarioDAO.cambiarEstado(idUsuario, activo));
    }

    /** Verifica el vínculo padre–hijo contra la base de datos (RN-08). */
    public boolean esHijoDe(int idPadre, int idEstudiante) {
        return consultar(() -> usuarioDAO.esHijoDe(idPadre, idEstudiante));
    }

    private void validarDatos(String nombres, String apellidos, String correo, Rol rol) {
        if (!Validador.esTextoValido(nombres, 200) || !Validador.esTextoValido(apellidos, 200)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "Complete los nombres y apellidos del usuario.");
        }
        if (!Validador.esCorreoValido(correo)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "Ingrese un correo válido de hasta 150 caracteres.");
        }
        if (rol == null) {
            throw new NegocioException(CodigoError.DATO_INVALIDO, "Seleccione el rol del usuario.");
        }
    }

    @Override
    protected NegocioException traducir(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException
                || e.getErrorCode() == ERROR_CLAVE_DUPLICADA
                || e.getErrorCode() == ERROR_INDICE_DUPLICADO) {
            return new NegocioException(CodigoError.CONFLICTO,
                    "Ya existe un usuario con ese correo.", e);
        }
        return super.traducir(e);
    }
}
