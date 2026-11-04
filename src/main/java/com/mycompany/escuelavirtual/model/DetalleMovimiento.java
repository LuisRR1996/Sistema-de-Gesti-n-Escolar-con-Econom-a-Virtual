package com.mycompany.escuelavirtual.model;

import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimiento de billetera con el nombre del producto ya resuelto, para
 * mostrarlo en el historial sin consultas adicionales desde la vista.
 */
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
