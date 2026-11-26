package com.mycompany.escuelavirtual.model;

import com.mycompany.escuelavirtual.model.enums.EstadoRecarga;
import com.mycompany.escuelavirtual.model.enums.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recarga con los nombres del padre y del estudiante ya resueltos, para la
 * bandeja de aprobación y para el historial del padre.
 */
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
