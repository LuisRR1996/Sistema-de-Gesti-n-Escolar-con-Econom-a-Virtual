package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.CuadreBilletera;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a {@code billeteras}.
 *
 * <p>Los métodos que reciben una {@link Connection} participan en la
 * transacción que abre el servicio; los que no la reciben son consultas
 * sueltas de solo lectura.</p>
 */
public class BilleteraDAO extends BaseDAO {

    private static final String COLUMNAS =
            "id_billetera, id_estudiante, saldo_dinero, saldo_monedas";

    /**
     * Devuelve la billetera del estudiante y la crea con saldos en cero si
     * todavía no existe. Pensado para pantallas de consulta.
     */
    public Billetera obtenerOCrearPorEstudiante(int idEstudiante) throws SQLException {
        try (Connection cn = connection()) {
            Billetera billetera = obtenerPorEstudiante(cn, idEstudiante);
            return billetera != null ? billetera : crear(cn, idEstudiante);
        }
    }

    public Billetera obtenerPorEstudiante(Connection cn, int idEstudiante) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM billeteras WHERE id_estudiante = ?";
        return consultarUna(cn, sql, idEstudiante);
    }

    /**
     * Igual que {@link #obtenerPorEstudiante} pero reservando la fila hasta el
     * final de la transacción, para que dos compras simultáneas no lean el
     * mismo saldo.
     */
    public Billetera bloquearPorEstudiante(Connection cn, int idEstudiante) throws SQLException {
        String sql = "SELECT " + COLUMNAS
                + " FROM billeteras WITH (UPDLOCK, ROWLOCK) WHERE id_estudiante = ?";
        return consultarUna(cn, sql, idEstudiante);
    }

    public Billetera crear(Connection cn, int idEstudiante) throws SQLException {
        String sql = "INSERT INTO billeteras (id_estudiante, saldo_dinero, saldo_monedas) VALUES (?, 0.00, 0)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEstudiante);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int idGenerado = keys.next() ? keys.getInt(1) : 0;
                return new Billetera(idGenerado, idEstudiante, new BigDecimal("0.00"), 0);
            }
        }
    }

    /** Devuelve la billetera del estudiante, creándola dentro de la transacción si falta. */
    public Billetera obtenerOCrear(Connection cn, int idEstudiante) throws SQLException {
        Billetera billetera = obtenerPorEstudiante(cn, idEstudiante);
        return billetera != null ? billetera : crear(cn, idEstudiante);
    }

    public void descontarDinero(Connection cn, int idBilletera, BigDecimal monto) throws SQLException {
        actualizarDinero(cn, idBilletera, monto.negate());
    }

    public void acreditarDinero(Connection cn, int idBilletera, BigDecimal monto) throws SQLException {
        actualizarDinero(cn, idBilletera, monto);
    }

    public void descontarMonedas(Connection cn, int idBilletera, int monedas) throws SQLException {
        actualizarMonedas(cn, idBilletera, -monedas);
    }

    public void acreditarMonedas(Connection cn, int idBilletera, int monedas) throws SQLException {
        actualizarMonedas(cn, idBilletera, monedas);
    }

    private void actualizarDinero(Connection cn, int idBilletera, BigDecimal delta) throws SQLException {
        String sql = "UPDATE billeteras SET saldo_dinero = saldo_dinero + ? WHERE id_billetera = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, delta);
            ps.setInt(2, idBilletera);
            ps.executeUpdate();
        }
    }

    private void actualizarMonedas(Connection cn, int idBilletera, int delta) throws SQLException {
        String sql = "UPDATE billeteras SET saldo_monedas = saldo_monedas + ? WHERE id_billetera = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, idBilletera);
            ps.executeUpdate();
        }
    }

    /**
     * Lee la vista de conciliación {@code vw_cuadre_billeteras}: saldo
     * almacenado frente a saldo recalculado (ADR-04, CP-20).
     */
    public List<CuadreBilletera> listarCuadre() throws SQLException {
        String sql = """
                SELECT id_billetera, id_estudiante, saldo_dinero, dinero_calculado,
                       saldo_monedas, monedas_calculadas
                  FROM vw_cuadre_billeteras
                 ORDER BY id_billetera""";
        List<CuadreBilletera> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new CuadreBilletera(
                        rs.getInt("id_billetera"),
                        rs.getInt("id_estudiante"),
                        rs.getBigDecimal("saldo_dinero"),
                        rs.getBigDecimal("dinero_calculado"),
                        rs.getInt("saldo_monedas"),
                        rs.getBigDecimal("monedas_calculadas")));
            }
        }
        return lista;
    }

    private Billetera consultarUna(Connection cn, String sql, int idEstudiante) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Billetera(
                        rs.getInt("id_billetera"),
                        rs.getInt("id_estudiante"),
                        rs.getBigDecimal("saldo_dinero"),
                        rs.getInt("saldo_monedas"));
            }
        }
    }
}
