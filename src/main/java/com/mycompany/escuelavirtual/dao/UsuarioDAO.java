package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Usuario> listarPorRol(Rol rol) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE rol = ? AND estado = 1 ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, rol.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Usuario> listarHijosPorPadre(int idPadre) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE rol = 'ESTUDIANTE' AND id_padre = ? AND estado = 1 ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPadre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Usuario insertar(Usuario u) throws SQLException {
        String sql = """
                INSERT INTO usuarios (nombres, apellidos, correo, password_hash, rol, grado, seccion, id_padre, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombres().trim());
            ps.setString(2, u.getApellidos().trim());
            ps.setString(3, u.getCorreo().trim().toLowerCase());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getRol().name());
            if (u.getRol() == Rol.ESTUDIANTE && u.getGrado() != null && !u.getGrado().isBlank()) {
                ps.setString(6, u.getGrado().trim());
            } else {
                ps.setNull(6, Types.NVARCHAR);
            }
            if (u.getRol() == Rol.ESTUDIANTE && u.getSeccion() != null && !u.getSeccion().isBlank()) {
                ps.setString(7, u.getSeccion().trim());
            } else {
                ps.setNull(7, Types.NVARCHAR);
            }
            if (u.getRol() == Rol.ESTUDIANTE && u.getIdPadre() != null && u.getIdPadre() > 0) {
                ps.setInt(8, u.getIdPadre());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setBoolean(9, u.isEstado());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setIdUsuario(keys.getInt(1));
                }
            }
            if (u.getRol() == Rol.ESTUDIANTE && u.getIdUsuario() != null) {
                asegurarBilletera(cn, u.getIdUsuario());
            }
        }
        return u;
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = """
                UPDATE usuarios
                   SET nombres = ?, apellidos = ?, correo = ?, rol = ?, grado = ?, seccion = ?, id_padre = ?, estado = ?
                 WHERE id_usuario = ?""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombres().trim());
            ps.setString(2, u.getApellidos().trim());
            ps.setString(3, u.getCorreo().trim().toLowerCase());
            ps.setString(4, u.getRol().name());
            if (u.getRol() == Rol.ESTUDIANTE && u.getGrado() != null && !u.getGrado().isBlank()) {
                ps.setString(5, u.getGrado().trim());
            } else {
                ps.setNull(5, Types.NVARCHAR);
            }
            if (u.getRol() == Rol.ESTUDIANTE && u.getSeccion() != null && !u.getSeccion().isBlank()) {
                ps.setString(6, u.getSeccion().trim());
            } else {
                ps.setNull(6, Types.NVARCHAR);
            }
            if (u.getRol() == Rol.ESTUDIANTE && u.getIdPadre() != null && u.getIdPadre() > 0) {
                ps.setInt(7, u.getIdPadre());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, u.isEstado());
            ps.setInt(9, u.getIdUsuario());
            ps.executeUpdate();

            if (u.getRol() == Rol.ESTUDIANTE) {
                asegurarBilletera(cn, u.getIdUsuario());
            }
        }
    }

    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET estado = ? WHERE id_usuario = ?";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    private void asegurarBilletera(Connection cn, int idEstudiante) throws SQLException {
        String check = "SELECT 1 FROM billeteras WHERE id_estudiante = ?";
        try (PreparedStatement ps = cn.prepareStatement(check)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String ins = "INSERT INTO billeteras (id_estudiante, saldo_dinero, saldo_monedas) VALUES (?, 0.00, 0)";
        try (PreparedStatement ps = cn.prepareStatement(ins)) {
            ps.setInt(1, idEstudiante);
            ps.executeUpdate();
        }
    }
    
    /**
     * @return el usuario con ese identificador o {@code null} si no existe
     */
    public Usuario buscarPorId(int idUsuario) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE id_usuario = ?";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Verifica el vínculo familiar contra {@code usuarios.id_padre} (RN-08).
     * Es la única fuente del permiso del padre: no se deduce de la interfaz.
     */
    public boolean esHijoDe(int idPadre, int idEstudiante) throws SQLException {
        String sql = """
                SELECT 1
                  FROM usuarios
                 WHERE id_usuario = ? AND id_padre = ? AND rol = 'ESTUDIANTE'""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idPadre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Administradores activos distintos del indicado. */
    public int contarAdminsActivosExcepto(int idUsuario) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                  FROM usuarios
                 WHERE rol = 'ADMIN' AND estado = 1 AND id_usuario <> ?""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

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
