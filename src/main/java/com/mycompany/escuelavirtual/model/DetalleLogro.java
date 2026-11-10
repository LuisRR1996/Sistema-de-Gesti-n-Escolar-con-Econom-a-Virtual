package com.mycompany.escuelavirtual.model;

import java.time.LocalDateTime;

/**
 * Logro con los nombres del estudiante y de quien lo otorgó ya resueltos, para
 * mostrarlo en una tabla sin que la vista tenga que consultar la base de datos.
 */
public record DetalleLogro(
        int idLogro,
        int idEstudiante,
        String nombreEstudiante,
        int idDocente,
        String nombreDocente,
        String tipo,
        String descripcion,
        int monedas,
        LocalDateTime fecha) {
}
