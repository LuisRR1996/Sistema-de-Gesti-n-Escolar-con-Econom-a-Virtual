package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.service.UsuarioService;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class UsuarioController {
    
    private final UsuarioService usuarioService = new UsuarioService();

    public List<Usuario> obtenerTodosLosUsuarios() {
        try {
            return usuarioService.listarTodos();
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Usuario> obtenerPorRol(Rol rol) {
        try {
            return usuarioService.listarPorRol(rol);
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios por rol: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Usuario> obtenerHijosPorPadre(int idPadre) {
        try {
            return usuarioService.listarHijosPorPadre(idPadre);
        } catch (SQLException e) {
            System.err.println("Error al listar hijos del padre: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Usuario registrarUsuario(String nombres, String apellidos, String correo, String clave,
            Rol rol, String grado, String seccion, Integer idPadre) throws SQLException {
        return usuarioService.registrar(nombres, apellidos, correo, clave, rol, grado, seccion, idPadre);
    }

    public void actualizarUsuario(Usuario u) throws SQLException {
        usuarioService.actualizar(u);
    }

    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        usuarioService.cambiarEstado(idUsuario, activo);
    }
    
}
