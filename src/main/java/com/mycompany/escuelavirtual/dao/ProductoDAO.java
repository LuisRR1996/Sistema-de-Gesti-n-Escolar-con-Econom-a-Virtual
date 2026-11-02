package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Producto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO extends BaseDAO {
    
    public List<Producto> listarTodos() throws SQLException {
        String sql = "SELECT id_producto, nombre, descripcion, precio_dinero, precio_monedas, stock, activo FROM productos ORDER BY id_producto";
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Producto> listarActivos() throws SQLException {
        String sql = "SELECT id_producto, nombre, descripcion, precio_dinero, precio_monedas, stock, activo FROM productos WHERE activo = 1 ORDER BY id_producto";
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Producto insertar(Producto p) throws SQLException {
        String sql = """
                INSERT INTO productos (nombre, descripcion, precio_dinero, precio_monedas, stock, activo)
                VALUES (?, ?, ?, ?, ?, ?)""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre().trim());
            ps.setString(2, p.getDescripcion() == null ? "" : p.getDescripcion().trim());
            if (p.getPrecioDinero() != null) {
                ps.setBigDecimal(3, p.getPrecioDinero());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (p.getPrecioMonedas() != null) {
                ps.setInt(4, p.getPrecioMonedas());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, p.getStock());
            ps.setBoolean(6, p.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setIdProducto(keys.getInt(1));
                }
            }
        }
        return p;
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = """
                UPDATE productos
                   SET nombre = ?, descripcion = ?, precio_dinero = ?, precio_monedas = ?, stock = ?, activo = ?
                 WHERE id_producto = ?""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre().trim());
            ps.setString(2, p.getDescripcion() == null ? "" : p.getDescripcion().trim());
            if (p.getPrecioDinero() != null) {
                ps.setBigDecimal(3, p.getPrecioDinero());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (p.getPrecioMonedas() != null) {
                ps.setInt(4, p.getPrecioMonedas());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, p.getStock());
            ps.setBoolean(6, p.isActivo());
            ps.setInt(7, p.getIdProducto());
            ps.executeUpdate();
        }
    }

    public void actualizarStock(int idProducto, int nuevoStock) throws SQLException {
        String sql = "UPDATE productos SET stock = ? WHERE id_producto = ?";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, nuevoStock));
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int idProducto, boolean activo) throws SQLException {
        String sql = "UPDATE productos SET activo = ? WHERE id_producto = ?";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    private static Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        BigDecimal dinero = rs.getBigDecimal("precio_dinero");
        p.setPrecioDinero(dinero);
        int monedas = rs.getInt("precio_monedas");
        p.setPrecioMonedas(rs.wasNull() ? null : monedas);
        p.setStock(rs.getInt("stock"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
    
}
