package com.mycompany.escuelavirtual.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones de formato compartidas por la interfaz y los servicios.
 *
 * <p>Los límites de longitud coinciden con el DDL: correo
 * {@code NVARCHAR(150)} y montos {@code DECIMAL(10,2)}. Las validaciones no
 * consultan la base de datos; solo comprueban el formato.</p>
 */
public final class Validador {

    private static final Pattern CORREO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int CORREO_MAX = 150;
    private static final int DINERO_ENTEROS_MAX = 8; // DECIMAL(10,2) -> 8 dígitos enteros
    private static final int DINERO_ESCALA_MAX = 2;

    private Validador() {
        // Clase de utilidad.
    }

    /** Normaliza un correo: recorta espacios y pasa a minúsculas. */
    public static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean esCorreoValido(String correo) {
        String normalizado = normalizarCorreo(correo);
        return normalizado != null
                && normalizado.length() <= CORREO_MAX
                && CORREO.matcher(normalizado).matches();
    }

    public static boolean esTextoValido(String valor) {
        return valor != null && !valor.isBlank();
    }

    public static boolean esTextoValido(String valor, int longitudMaxima) {
        return esTextoValido(valor) && valor.trim().length() <= longitudMaxima;
    }

    /**
     * Un monto válido es positivo, con dos decimales como máximo y que cabe en
     * {@code DECIMAL(10,2)}.
     */
    public static boolean esMontoDineroValido(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            return false;
        }
        if (monto.scale() > DINERO_ESCALA_MAX) {
            return false;
        }
        int enteros = monto.precision() - monto.scale();
        return enteros <= DINERO_ENTEROS_MAX;
    }

    public static boolean esMonedasValidas(Integer monedas) {
        return monedas != null && monedas > 0;
    }

    public static boolean esCantidadValida(Integer cantidad) {
        return cantidad != null && cantidad > 0;
    }

    public static boolean esIdValido(Integer id) {
        return id != null && id > 0;
    }
}
