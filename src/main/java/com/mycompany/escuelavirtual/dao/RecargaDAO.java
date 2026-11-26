package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.model.DetalleRecarga;
import com.mycompany.escuelavirtual.model.Recarga;
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

/**
 * Acceso a {@code recargas}.
 *
 * <p>La aprobación se apoya en {@link #bloquearPorId}: reserva la fila de la
 * recarga dentro de la transacción del servicio, de modo que dos
 * administradores que aprueben a la vez no puedan acreditar dos veces
 * (RN-06).</p>
 */
public class RecargaDAO extends BaseDAO {

    private static final String SELECCION = """
            SELECT r.id_recarga, r.id_billetera, b.id_estudiante,
                   (p.nombres + ' ' + p.apellidos) AS nombre_padre,
                   (e.nombres + ' ' + e.apellidos) AS nombre_estudiante,
                   r.monto, r.metodo_pago, r.estado, r.fecha
              FROM recargas r
              JOIN billeteras b ON r.id_billetera = b.id_billetera
              JOIN usuarios e   ON b.id_estudiante = e.id_usuario
              JOIN usuarios p   ON r.id_padre = p.id_usuario""";

    private static final String ORDEN = " ORDER BY r.fecha DESC, r.id_recarga DESC";

    public List<DetalleRecarga> listarTodas() throws SQLException {
        List<DetalleRecarga> lista = new ArrayList<>();
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(SELECCION + ORDEN);
                ResultSet rs = ps.executeQuery()) {
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
                        ts == null ? LocalDateTime.now() : ts.toLocalDateTime()));
            }
        }
        return lista;
    }

    /** Registra la solicitud en estado PENDIENTE: todavía no toca el saldo (RN-06). */
    public void insertar(Connection cn, int idBilletera, int idPadre,
            BigDecimal monto, MetodoPago metodo) throws SQLException {
        String sql = """
                INSERT INTO recargas (id_billetera, id_padre, monto, metodo_pago, estado, fecha)
                VALUES (?, ?, ?, ?, 'PENDIENTE', SYSDATETIME())""";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idBilletera);
            ps.setInt(2, idPadre);
            ps.setBigDecimal(3, monto);
            ps.setString(4, metodo.name());
            ps.executeUpdate();
        }
    }

    /**
     * Lee la recarga reservando su fila hasta el final de la transacción.
     *
     * @return la recarga con su estado actual, o {@code null} si no existe
     */
    public Recarga bloquearPorId(Connection cn, int idRecarga) throws SQLException {
        String sql = """
                SELECT id_recarga, id_billetera, id_padre, monto, metodo_pago, estado, fecha
                  FROM recargas WITH (UPDLOCK, ROWLOCK)
                 WHERE id_recarga = ?""";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRecarga);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Recarga r = new Recarga();
                r.setIdRecarga(rs.getInt("id_recarga"));
                r.setIdBilletera(rs.getInt("id_billetera"));
                r.setIdPadre(rs.getInt("id_padre"));
                r.setMonto(rs.getBigDecimal("monto"));
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setEstado(EstadoRecarga.valueOf(rs.getString("estado")));
                Timestamp ts = rs.getTimestamp("fecha");
                r.setFecha(ts == null ? null : ts.toLocalDateTime());
                return r;
            }
        }
    }

    public void aprobar(Connection cn, int idRecarga, int idAdmin) throws SQLException {
        String sql = """
                UPDATE recargas
                   SET estado = 'APROBADA',
                       id_admin_aprueba = ?,
                       fecha_aprobacion = SYSDATETIME()
                 WHERE id_recarga = ? AND estado = 'PENDIENTE'""";
        actualizarEstado(cn, sql, idAdmin, idRecarga, null);
    }

    public void rechazar(Connection cn, int idRecarga, int idAdmin, String motivo) throws SQLException {
        String sql = """
                UPDATE recargas
                   SET estado = 'RECHAZADA',
                       id_admin_aprueba = ?,
                       fecha_aprobacion = SYSDATETIME(),
                       motivo_rechazo = ?
                 WHERE id_recarga = ? AND estado = 'PENDIENTE'""";
        actualizarEstado(cn, sql, idAdmin, idRecarga, motivo);
    }

    private void actualizarEstado(Connection cn, String sql, int idAdmin, int idRecarga, String motivo)
            throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAdmin);
            if (motivo == null) {
                ps.setInt(2, idRecarga);
            } else {
                ps.setString(2, motivo);
                ps.setInt(3, idRecarga);
            }
            ps.executeUpdate();
        }
    }
    
    /**
     * Cuenta cuántas recargas ha solicitado un padre en el día actual en SQL
     * Server.
     */
    public int contarRecargasDelDiaPorPadre(int idPadre) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                  FROM recargas
                 WHERE id_padre = ? AND CAST(fecha AS DATE) = CAST(SYSDATETIME() AS DATE)""";
        try (Connection cn = connection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPadre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    
}
