package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plomería común de los servicios.
 *
 * <p>Los DAO lanzan {@link SQLException}; la interfaz solo debe ver una
 * {@link NegocioException} con un {@link CodigoError} estable. Aquí se hace esa
 * traducción una sola vez, siguiendo el patrón que ya usa
 * {@code AuthService}.</p>
 */
abstract class ServicioBase {

    @FunctionalInterface
    protected interface Consulta<T> {
        T ejecutar() throws SQLException;
    }

    @FunctionalInterface
    protected interface Operacion {
        void ejecutar() throws SQLException;
    }

    protected <T> T consultar(Consulta<T> consulta) {
        try {
            return consulta.ejecutar();
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    protected void ejecutar(Operacion operacion) {
        try {
            operacion.ejecutar();
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    /** Las subclases la amplían cuando un error técnico concreto tiene lectura de negocio. */
    protected NegocioException traducir(SQLException e) {
        Logger.getLogger(getClass().getName())
                .log(Level.SEVERE, "Error de base de datos", e);
        return new NegocioException(CodigoError.SIN_CONEXION, CodigoError.SIN_CONEXION.getMensaje(), e);
    }
}
