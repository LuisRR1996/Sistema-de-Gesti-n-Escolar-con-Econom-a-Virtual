package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.service.UsuarioService;
import java.util.List;

/**
 * Orquesta la gestión de usuarios entre la vista y {@link UsuarioService} (M02).
 *
 * <p>El controlador fija el actor de la sesión en cada llamada, de modo que la
 * vista no pueda elegir en nombre de quién actúa. Las reglas y el control de
 * acceso viven en el servicio y en {@code Permisos}: así ninguna ruta —ni la
 * interfaz, ni una prueba, ni otro controlador— puede esquivarlos. Los errores
 * de negocio suben como {@code NegocioException}.</p>
 */
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final Usuario actor;

    public UsuarioController(Usuario actor) {
        this(new UsuarioService(), actor);
    }

    public UsuarioController(UsuarioService usuarioService, Usuario actor) {
        this.usuarioService = usuarioService;
        this.actor = actor;
    }

    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos(actor);
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioService.listarPorRol(actor, rol);
    }

    public List<Usuario> listarHijos() {
        return usuarioService.listarHijos(actor, actor.getIdUsuario());
    }

    public Usuario registrar(String nombres, String apellidos, String correo, String clave,
            Rol rol, String grado, String seccion, Integer idPadre) {
        return usuarioService.registrar(actor, nombres, apellidos, correo, clave, rol, grado, seccion, idPadre);
    }

    public void actualizar(Usuario usuario) {
        usuarioService.actualizar(actor, usuario);
    }

    public void cambiarEstado(int idUsuario, boolean activo) {
        usuarioService.cambiarEstado(actor, idUsuario, activo);
    }
}
