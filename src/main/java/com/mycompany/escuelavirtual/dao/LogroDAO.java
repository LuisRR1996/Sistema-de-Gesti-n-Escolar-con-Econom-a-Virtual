package com.mycompany.escuelavirtual.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogroDAO extends BaseDAO {
    
    public record DetalleLogro(
            int idLogro,
            int idEstudiante,
            String nombreEstudiante,
            int idDocente,
            String nombreDocente,
            String tipo,
            String descripcion,
            int monedas,
            LocalDateTime fecha) {

    }

    public List<DetalleLogro> listarTodos() throws SQLException {
        String sql = """
                SELECT l.id_logro, l.id_estudiante,
                       (e.nombres + ' ' + e.apellidos) AS nombre_estudiante,
                       l.id_docente,
                       (d.nombres + ' ' + d.apellidos) AS nombre_docente,
                       l.tipo, l.descripcion, l.monedas, l.fecha
                  FROM logros l
                  JOIN usuarios e ON l.id_estudiante = e.id_usuario
                  LEFT JOIN usuarios d ON l.id_docente = d.id_usuario
                 ORDER BY l.fecha DESC, l.id_logro DESC""";
        List<DetalleLogro> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<DetalleLogro> listarPorEstudiante(int idEstudiante) throws SQLException {
        String sql = """
                SELECT l.id_logro, l.id_estudiante,
                       (e.nombres + ' ' + e.apellidos) AS nombre_estudiante,
                       l.id_docente,
                       (d.nombres + ' ' + d.apellidos) AS nombre_docente,
                       l.tipo, l.descripcion, l.monedas, l.fecha
                  FROM logros l
                  JOIN usuarios e ON l.id_estudiante = e.id_usuario
                  LEFT JOIN usuarios d ON l.id_docente = d.id_usuario
                 WHERE l.id_estudiante = ?
                 ORDER BY l.fecha DESC, l.id_logro DESC""";
        List<DetalleLogro> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void otorgarLogro(int idEstudiante, int idUsuarioOtorga, String tipo, String descripcion, int monedas) throws SQLException {
        new BilleteraDAO().obtenerOCrearPorEstudiante(idEstudiante);
        try (Connection cn = connection()) {
            cn.setAutoCommit(false);
            try {
                int idDocenteValido = resolverIdDocente(cn, idUsuarioOtorga);
                int monedasAntes = leerSaldoMonedas(cn, idEstudiante);

                String ins = """
                        INSERT INTO logros (id_estudiante, id_docente, tipo, descripcion, monedas, fecha)
                        VALUES (?, ?, ?, ?, ?, SYSDATETIME())""";
                try (PreparedStatement ps = cn.prepareStatement(ins)) {
                    ps.setInt(1, idEstudiante);
                    ps.setInt(2, idDocenteValido);
                    ps.setString(3, tipo);
                    ps.setString(4, descripcion);
                    ps.setInt(5, monedas);
                    ps.executeUpdate();
                }

                int monedasDespues = leerSaldoMonedas(cn, idEstudiante);
                if (monedasDespues == monedasAntes) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE billeteras SET saldo_monedas = saldo_monedas + ? WHERE id_estudiante = ?")) {
                        ps.setInt(1, monedas);
                        ps.setInt(2, idEstudiante);
                        ps.executeUpdate();
                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private int resolverIdDocente(Connection cn, int idPreferido) throws SQLException {
        if (idPreferido > 0) {
            try (PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM usuarios WHERE id_usuario = ? AND rol = 'DOCENTE'")) {
                ps.setInt(1, idPreferido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return idPreferido;
                    }
                }
            }
        }
        try (PreparedStatement ps = cn.prepareStatement("SELECT TOP 1 id_usuario FROM usuarios WHERE rol = 'DOCENTE' ORDER BY id_usuario"); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : Math.max(1, idPreferido);
        }
    }

    private int leerSaldoMonedas(Connection cn, int idEstudiante) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT saldo_monedas FROM billeteras WHERE id_estudiante = ?")) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
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
                ts == null ? LocalDateTime.now() : ts.toLocalDateTime()
        );
    }
    
}
