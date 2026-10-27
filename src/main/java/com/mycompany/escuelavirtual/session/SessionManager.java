package com.mycompany.escuelavirtual.session;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;

public final class SessionManager {
    private Usuario usuario;

    public void iniciar(Usuario u) {
        usuario = java.util.Objects.requireNonNull(u, "usuario");
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Integer getUsuarioId() {
        return usuario == null ? null : usuario.getIdUsuario();
    }

    public Rol getRol() {
        return usuario == null ? null : usuario.getRol();
    }

    public boolean haySesionActiva() {
        return usuario != null;
    }

    public boolean tieneRol(Rol rol) {
        return usuario != null && usuario.getRol() == rol;
    }

    public void cerrar() {
        usuario = null;
    }
}
