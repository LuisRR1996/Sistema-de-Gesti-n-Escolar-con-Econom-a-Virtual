package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.DetalleMovimiento;
import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a {@code movimientos}: el registro auditable de toda compra o canje
 * (RN-05), siempre con el usuario que ejecutó la operación (RN-14).
 */
public class MovimientoDAO extends BaseDAO {

    public List<DetalleMovimiento> listarPorEstudiante(int idEstudiante) throws SQLException {
        String sql = """
                SELECT m.id_movimiento, m.id_billetera, m.id_producto, p.nombre AS nombre_producto,
                       m.metodo_pago, m.cantidad, m.monto_total, m.fecha
                  FROM movimientos m
                  JOIN billeteras b ON m.id_billetera = b.id_billetera
                  JOIN productos p  ON m.id_producto = p.id_producto
                 WHERE b.id_estudiante = ?
                 ORDER BY m.fecha DESC, m.id_movimiento DESC""";
        List<DetalleMovimiento> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha");
                    lista.add(new DetalleMovimiento(
                            rs.getInt("id_movimiento"),
                            rs.getInt("id_billetera"),
                            rs.getInt("id_producto"),
                            rs.getString("nombre_producto"),
                            TipoMoneda.valueOf(rs.getString("metodo_pago")),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("monto_total"),
                            ts == null ? LocalDateTime.now() : ts.toLocalDateTime()));
                }
            }
        }
        return lista;
    }

    public void insertar(Connection cn, int idBilletera, int idProducto, TipoMoneda metodo,
            int cantidad, BigDecimal montoTotal, int idUsuario) throws SQLException {
        String sql = """
                INSERT INTO movimientos (id_billetera, id_producto, metodo_pago, cantidad, monto_total, fecha, id_usuario)
                VALUES (?, ?, ?, ?, ?, SYSDATETIME(), ?)""";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idBilletera);
            ps.setInt(2, idProducto);
            ps.setString(3, metodo.name());
            ps.setInt(4, cantidad);
            ps.setBigDecimal(5, montoTotal);
            ps.setInt(6, idUsuario);
            ps.executeUpdate();
        }
    }
}
