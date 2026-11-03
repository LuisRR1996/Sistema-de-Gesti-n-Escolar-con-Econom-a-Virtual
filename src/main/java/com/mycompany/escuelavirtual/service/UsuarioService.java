package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.PasswordUtil;
import java.sql.SQLException;
import java.util.List;

public class UsuarioService {
    
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public List<Usuario> listarPorRol(Rol rol) throws SQLException {
        return usuarioDAO.listarPorRol(rol);
    }

    public List<Usuario> listarHijosPorPadre(int idPadre) throws SQLException {
        return usuarioDAO.listarHijosPorPadre(idPadre);
    }

    public Usuario registrar(String nombres, String apellidos, String correo, String clavePlana,
            Rol rol, String grado, String seccion, Integer idPadre) throws SQLException {
        Usuario u = new Usuario();
        u.setNombres(nombres.trim());
        u.setApellidos(apellidos.trim());
        u.setCorreo(correo.trim().toLowerCase());
        u.setPasswordHash(PasswordUtil.hash(clavePlana));
        u.setRol(rol);
        u.setGrado(rol == Rol.ESTUDIANTE ? grado : null);
        u.setSeccion(rol == Rol.ESTUDIANTE ? seccion : null);
        u.setIdPadre(rol == Rol.ESTUDIANTE ? idPadre : null);
        u.setEstado(true);
        return usuarioDAO.insertar(u);
    }

    public void actualizar(Usuario u) throws SQLException {
        usuarioDAO.actualizar(u);
    }

    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        usuarioDAO.cambiarEstado(idUsuario, activo);
    }
    
}
