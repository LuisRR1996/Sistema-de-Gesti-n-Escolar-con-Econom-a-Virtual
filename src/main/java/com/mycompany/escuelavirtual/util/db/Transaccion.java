package com.mycompany.escuelavirtual.util.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta varias sentencias en una única transacción JDBC (RN-07).
 *
 * <p>El dueño de la transacción es el servicio: abre la conexión, entrega la
 * misma {@link Connection} a todos los DAO que participan y confirma solo si
 * la operación termina bien. Cualquier excepción —incluida una
 * {@code NegocioException} de validación, como «saldo insuficiente»— deshace
 * todo el bloque.</p>
 */
public final class Transaccion {

    @FunctionalInterface
    public interface Operacion<T> {
        T ejecutar(Connection cn) throws SQLException;
    }

    @FunctionalInterface
    public interface Tarea {
        void ejecutar(Connection cn) throws SQLException;
    }

    private Transaccion() {
        // Clase de utilidad.
    }

    public static <T> T consultar(Operacion<T> operacion) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(cn);
                cn.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public static void ejecutar(Tarea tarea) throws SQLException {
        consultar(cn -> {
            tarea.ejecutar(cn);
            return null;
        });
    }
}
