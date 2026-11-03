package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.enums.EstadoRecarga;
import com.mycompany.escuelavirtual.model.enums.MetodoPago;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecargaDAO extends BaseDAO {
    
    public record DetalleRecarga(
            int idRecarga,
            int idBilletera,
            int idEstudiante,
            String nombrePadre,
            String nombreEstudiante,
            BigDecimal monto,
            MetodoPago metodo,
            EstadoRecarga estado,
            LocalDateTime fecha) {

    }

    public List<DetalleRecarga> listarTodas() throws SQLException {
        String sql = """
                SELECT r.id_recarga, r.id_billetera, b.id_estudiante,
                       (p.nombres + ' ' + p.apellidos) AS nombre_padre,
                       (e.nombres + ' ' + e.apellidos) AS nombre_estudiante,
                       r.monto, r.metodo_pago, r.estado, r.fecha
                  FROM recargas r
                  JOIN billeteras b ON r.id_billetera = b.id_billetera
                  JOIN usuarios e   ON b.id_estudiante = e.id_usuario
                  JOIN usuarios p   ON r.id_padre = p.id_usuario
                 ORDER BY r.fecha DESC, r.id_recarga DESC""";
        List<DetalleRecarga> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("fecha");
                lista.add(new DetalleRecarga(
                        rs.getInt("id_recarga"),
                        rs.getInt("id_billetera"),
                        rs.getInt("id_estudiante"),
                        rs.getString("nombre_padre"),
                        rs.getString("nombre_estudiante"),
                        rs.getBigDecimal("monto"),
                        MetodoPago.valueOf(rs.getString("metodo_pago")),
                        EstadoRecarga.valueOf(rs.getString("estado")),
                        ts == null ? LocalDateTime.now() : ts.toLocalDateTime()
                ));
            }
        }
        return lista;
    }

    public void registrarRecarga(int idPadre, int idEstudiante, BigDecimal monto, MetodoPago metodo) throws SQLException {
        Billetera b = new BilleteraDAO().obtenerOCrearPorEstudiante(idEstudiante);
        String sql = """
                INSERT INTO recargas (id_billetera, id_padre, monto, metodo_pago, estado, fecha)
                VALUES (?, ?, ?, ?, 'PENDIENTE', SYSDATETIME())""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, b.getIdBilletera());
            ps.setInt(2, idPadre);
            ps.setBigDecimal(3, monto);
            ps.setString(4, metodo.name());
            ps.executeUpdate();
        }
    }

    public void aprobarRecarga(int idRecarga, int idAdmin) throws SQLException {
        try (Connection cn = connection()) {
            cn.setAutoCommit(false);
            try {
                int idBilletera;
                BigDecimal monto;
                BigDecimal saldoAntes;

                String q = """
                        SELECT r.id_billetera, r.monto, b.saldo_dinero
                          FROM recargas r
                          JOIN billeteras b ON r.id_billetera = b.id_billetera
                         WHERE r.id_recarga = ? AND r.estado = 'PENDIENTE'""";
                try (PreparedStatement ps = cn.prepareStatement(q)) {
                    ps.setInt(1, idRecarga);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            cn.rollback();
                            return;
                        }
                        idBilletera = rs.getInt("id_billetera");
                        monto = rs.getBigDecimal("monto");
                        saldoAntes = rs.getBigDecimal("saldo_dinero");
                    }
                }

                String upd = """
                        UPDATE recargas
                           SET estado = 'APROBADA',
                               id_admin_aprueba = ?,
                               fecha_aprobacion = SYSDATETIME()
                         WHERE id_recarga = ?""";
                try (PreparedStatement ps = cn.prepareStatement(upd)) {
                    ps.setInt(1, idAdmin);
                    ps.setInt(2, idRecarga);
                    ps.executeUpdate();
                }

                BigDecimal saldoDespues = leerSaldoDinero(cn, idBilletera);
                if (saldoDespues.compareTo(saldoAntes) == 0) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE billeteras SET saldo_dinero = saldo_dinero + ? WHERE id_billetera = ?")) {
                        ps.setBigDecimal(1, monto);
                        ps.setInt(2, idBilletera);
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

    public void rechazarRecarga(int idRecarga, int idAdmin, String motivo) throws SQLException {
        String sql = """
                UPDATE recargas
                   SET estado = 'RECHAZADA',
                       id_admin_aprueba = ?,
                       fecha_aprobacion = SYSDATETIME(),
                       motivo_rechazo = ?
                 WHERE id_recarga = ? AND estado = 'PENDIENTE'""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAdmin);
            ps.setString(2, motivo == null ? "Rechazada por administración" : motivo);
            ps.setInt(3, idRecarga);
            ps.executeUpdate();
        }
    }

    private BigDecimal leerSaldoDinero(Connection cn, int idBilletera) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT saldo_dinero FROM billeteras WHERE id_billetera = ?")) {
            ps.setInt(1, idBilletera);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }
    
}
