package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Billetera;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BilleteraDAO extends BaseDAO {
    
    public Billetera obtenerOCrearPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT id_billetera, id_estudiante, saldo_dinero, saldo_monedas FROM billeteras WHERE id_estudiante = ?";
        try (Connection cn = connection()) {
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idEstudiante);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Billetera(
                                rs.getInt("id_billetera"),
                                rs.getInt("id_estudiante"),
                                rs.getBigDecimal("saldo_dinero"),
                                rs.getInt("saldo_monedas")
                        );
                    }
                }
            }
            String ins = "INSERT INTO billeteras (id_estudiante, saldo_dinero, saldo_monedas) VALUES (?, 0.00, 0)";
            try (PreparedStatement ps = cn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idEstudiante);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    int idGenerado = keys.next() ? keys.getInt(1) : 0;
                    return new Billetera(idGenerado, idEstudiante, new BigDecimal("0.00"), 0);
                }
            }
        }
    }
    
}
