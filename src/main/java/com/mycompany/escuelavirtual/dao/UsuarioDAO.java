package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Acceso a la tabla {@code usuarios}. Solo lee y escribe filas: las reglas de
 * bloqueo y de permisos viven en los servicios.
 */
public class UsuarioDAO extends BaseDAO {

    private static final String COLUMNAS = """
            id_usuario, nombres, apellidos, correo, password_hash, rol,
            grado, seccion, id_padre, estado, intentos_fallidos, bloqueado_hasta, ultimo_acceso""";

    /**
     * Busca por correo normalizado (sin espacios y en minúsculas).
     *
     * @return el usuario o {@code null} si no existe
     */
    public Usuario findByCorreo(String correo) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE LOWER(correo) = ?";
        try (Connection cn = connection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo == null ? "" : correo.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Suma un intento fallido y, al llegar al máximo, bloquea la cuenta durante
     * los minutos indicados. Se hace en una sola sentencia para que dos
     * intentos simultáneos no pierdan la cuenta.
     *
     * @return {@code true} si la cuenta quedó bloqueada con este intento
     */
    public boolean registrarIntentoFallido(int idUsuario, int maxIntentos, int minutosBloqueo)
            throws SQLException {
        String sql = """
                UPDATE usuarios
                   SET intentos_fallidos = CASE WHEN intentos_fallidos + 1 >= ? THEN 0
                                                ELSE intentos_fallidos + 1 END,
                       bloqueado_hasta   = CASE WHEN intentos_fallidos + 1 >= ?
                                                THEN DATEADD(MINUTE, ?, SYSDATETIME())
                                                ELSE bloqueado_hasta END
                 WHERE id_usuario = ?""";
        try (Connection cn = connection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maxIntentos);
            ps.setInt(2, maxIntentos);
            ps.setInt(3, minutosBloqueo);
            ps.setInt(4, idUsuario);
            ps.executeUpdate();
        }
        return contarIntentos(idUsuario) == 0 && estaBloqueadoEnServidor(idUsuario);
    }

    /** Limpia intentos y bloqueo, y deja constancia del acceso. */
    public void registrarAccesoExitoso(int idUsuario) throws SQLException {
        String sql = """
                UPDATE usuarios
                   SET intentos_fallidos = 0,
                       bloqueado_hasta = NULL,
                       ultimo_acceso = SYSDATETIME()
                 WHERE id_usuario = ?""";
        try (Connection cn = connection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    /** Hora del servidor: evita depender del reloj del equipo cliente. */
    public LocalDateTime ahoraEnServidor() throws SQLException {
        try (Connection cn = connection();
                PreparedStatement ps = cn.prepareStatement("SELECT SYSDATETIME()");
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getTimestamp(1).toLocalDateTime() : LocalDateTime.now();
        }
    }

    private int contarIntentos(int idUsuario) throws SQLException {
        try (Connection cn = connection();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT intentos_fallidos FROM usuarios WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private boolean estaBloqueadoEnServidor(int idUsuario) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE id_usuario = ? AND bloqueado_hasta > SYSDATETIME()";
        try (Connection cn = connection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombres(rs.getString("nombres"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setRol(Rol.valueOf(rs.getString("rol")));
        usuario.setGrado(rs.getString("grado"));
        usuario.setSeccion(rs.getString("seccion"));
        int idPadre = rs.getInt("id_padre");
        usuario.setIdPadre(rs.wasNull() ? null : idPadre);
        usuario.setEstado(rs.getBoolean("estado"));
        usuario.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        usuario.setBloqueadoHasta(aFechaHora(rs.getTimestamp("bloqueado_hasta")));
        usuario.setUltimoAcceso(aFechaHora(rs.getTimestamp("ultimo_acceso")));
        return usuario;
    }

    private static LocalDateTime aFechaHora(Timestamp valor) {
        return valor == null ? null : valor.toLocalDateTime();
    }
}
