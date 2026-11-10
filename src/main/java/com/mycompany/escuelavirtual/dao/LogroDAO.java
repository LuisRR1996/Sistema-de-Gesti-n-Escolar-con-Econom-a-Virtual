package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.DetalleLogro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a {@code logros}.
 *
 * <p>La inserción recibe la {@link Connection} del servicio porque el logro y
 * el abono de monedas en la billetera forman una sola transacción (RN-05).</p>
 */
public class LogroDAO extends BaseDAO {

    private static final String SELECCION = """
            SELECT l.id_logro, l.id_estudiante,
                   (e.nombres + ' ' + e.apellidos) AS nombre_estudiante,
                   l.id_docente,
                   (d.nombres + ' ' + d.apellidos) AS nombre_docente,
                   l.tipo, l.descripcion, l.monedas, l.fecha
              FROM logros l
              JOIN usuarios e ON l.id_estudiante = e.id_usuario
              LEFT JOIN usuarios d ON l.id_docente = d.id_usuario""";

    private static final String ORDEN = " ORDER BY l.fecha DESC, l.id_logro DESC";

    public List<DetalleLogro> listarPorEstudiante(int idEstudiante) throws SQLException {
        return listar(SELECCION + " WHERE l.id_estudiante = ?" + ORDEN, idEstudiante);
    }

    /** Logros registrados por un docente o administrador concreto. */
    public List<DetalleLogro> listarPorOtorgante(int idOtorgante) throws SQLException {
        return listar(SELECCION + " WHERE l.id_docente = ?" + ORDEN, idOtorgante);
    }
    
    /**
     * Todos los logros registrados en el sistema (vista Admin).
     */
    public List<DetalleLogro> listarTodos() throws SQLException {
        List<DetalleLogro> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(SELECCION + ORDEN); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Inserta el logro atribuido a quien lo otorga realmente (RN-14); si el
     * identificador no existe en {@code usuarios}, la clave foránea rechaza la
     * operación y la transacción se deshace.
     */
    public void insertar(Connection cn, int idEstudiante, int idOtorgante,
            String tipo, String descripcion, int monedas) throws SQLException {
        String sql = """
                INSERT INTO logros (id_estudiante, id_docente, tipo, descripcion, monedas, fecha)
                VALUES (?, ?, ?, ?, ?, SYSDATETIME())""";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idOtorgante);
            ps.setString(3, tipo);
            ps.setString(4, descripcion);
            ps.setInt(5, monedas);
            ps.executeUpdate();
        }
    }

    private List<DetalleLogro> listar(String sql, int parametro) throws SQLException {
        List<DetalleLogro> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private static DetalleLogro mapear(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha");
        return new DetalleLogro(
                rs.getInt("id_logro"),
                rs.getInt("id_estudiante"),
                rs.getString("nombre_estudiante"),
                rs.getInt("id_docente"),
                rs.getString("nombre_docente"),
                rs.getString("tipo"),
                rs.getString("descripcion"),
                rs.getInt("monedas"),
                ts == null ? LocalDateTime.now() : ts.toLocalDateTime());
    }
}
