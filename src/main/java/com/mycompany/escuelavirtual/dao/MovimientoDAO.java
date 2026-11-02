package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Billetera;
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

public class MovimientoDAO extends BaseDAO {
    
    public record DetalleMovimiento(
            int idMovimiento,
            int idBilletera,
            int idProducto,
            String nombreProducto,
            TipoMoneda metodoPago,
            int cantidad,
            BigDecimal montoTotal,
            LocalDateTime fecha) {

    }

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
                            ts == null ? LocalDateTime.now() : ts.toLocalDateTime()
                    ));
                }
            }
        }
        return lista;
    }

    public void registrarCompraOCanje(int idEstudiante, int idProducto, TipoMoneda metodo, int cantidad, BigDecimal montoTotal) throws SQLException {
        Billetera billetera = new BilleteraDAO().obtenerOCrearPorEstudiante(idEstudiante);
        try (Connection cn = connection()) {
            cn.setAutoCommit(false);
            try {
                int stockAntes = leerStock(cn, idProducto);
                BigDecimal dineroAntes = leerDinero(cn, billetera.getIdBilletera());
                int monedasAntes = leerMonedas(cn, billetera.getIdBilletera());

                if (tieneColumnaIdUsuario(cn)) {
                    String ins = """
                            INSERT INTO movimientos (id_billetera, id_producto, metodo_pago, cantidad, monto_total, fecha, id_usuario)
                            VALUES (?, ?, ?, ?, ?, SYSDATETIME(), ?)""";
                    try (PreparedStatement ps = cn.prepareStatement(ins)) {
                        ps.setInt(1, billetera.getIdBilletera());
                        ps.setInt(2, idProducto);
                        ps.setString(3, metodo.name());
                        ps.setInt(4, cantidad);
                        ps.setBigDecimal(5, montoTotal);
                        ps.setInt(6, idEstudiante);
                        ps.executeUpdate();
                    }
                } else {
                    String ins = """
                            INSERT INTO movimientos (id_billetera, id_producto, metodo_pago, cantidad, monto_total, fecha)
                            VALUES (?, ?, ?, ?, ?, SYSDATETIME())""";
                    try (PreparedStatement ps = cn.prepareStatement(ins)) {
                        ps.setInt(1, billetera.getIdBilletera());
                        ps.setInt(2, idProducto);
                        ps.setString(3, metodo.name());
                        ps.setInt(4, cantidad);
                        ps.setBigDecimal(5, montoTotal);
                        ps.executeUpdate();
                    }
                }

                if (leerStock(cn, idProducto) == stockAntes) {
                    try (PreparedStatement ps = cn.prepareStatement("UPDATE productos SET stock = stock - ? WHERE id_producto = ?")) {
                        ps.setInt(1, cantidad);
                        ps.setInt(2, idProducto);
                        ps.executeUpdate();
                    }
                }

                if (metodo == TipoMoneda.DINERO && leerDinero(cn, billetera.getIdBilletera()).compareTo(dineroAntes) == 0) {
                    try (PreparedStatement ps = cn.prepareStatement("UPDATE billeteras SET saldo_dinero = saldo_dinero - ? WHERE id_billetera = ?")) {
                        ps.setBigDecimal(1, montoTotal);
                        ps.setInt(2, billetera.getIdBilletera());
                        ps.executeUpdate();
                    }
                } else if (metodo == TipoMoneda.MONEDAS && leerMonedas(cn, billetera.getIdBilletera()) == monedasAntes) {
                    try (PreparedStatement ps = cn.prepareStatement("UPDATE billeteras SET saldo_monedas = saldo_monedas - ? WHERE id_billetera = ?")) {
                        ps.setInt(1, montoTotal.intValue());
                        ps.setInt(2, billetera.getIdBilletera());
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

    private boolean tieneColumnaIdUsuario(Connection cn) throws SQLException {
        String sql = "SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.movimientos') AND name = 'id_usuario'";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private int leerStock(Connection cn, int idProducto) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT stock FROM productos WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private BigDecimal leerDinero(Connection cn, int idBilletera) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT saldo_dinero FROM billeteras WHERE id_billetera = ?")) {
            ps.setInt(1, idBilletera);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private int leerMonedas(Connection cn, int idBilletera) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT saldo_monedas FROM billeteras WHERE id_billetera = ?")) {
            ps.setInt(1, idBilletera);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    
}
