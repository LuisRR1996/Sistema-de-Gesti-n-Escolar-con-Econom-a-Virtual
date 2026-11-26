package com.mycompany.escuelavirtual.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Formato uniforme de dinero, monedas y fechas para toda la interfaz.
 */
public final class Formato {

    private static final Locale LOCALE_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_PE);
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PE);

    private Formato() {
        // Clase de utilidad.
    }

    /** Formatea un monto como moneda local, por ejemplo {@code S/ 50.00}. */
    public static String dinero(BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        return NumberFormat.getCurrencyInstance(LOCALE_PE).format(monto);
    }

    /** Formatea una cantidad de monedas virtuales. */
    public static String monedas(Integer monedas) {
        if (monedas == null) {
            return "";
        }
        return NumberFormat.getIntegerInstance(LOCALE_PE).format(monedas) + " monedas";
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : FECHA_HORA.format(fecha);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }
}
