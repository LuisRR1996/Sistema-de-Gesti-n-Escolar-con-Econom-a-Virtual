package com.mycompany.escuelavirtual.model;

import java.math.BigDecimal;

/**
 * Fila de la vista {@code vw_cuadre_billeteras}: compara el saldo almacenado
 * con el saldo recalculado a partir de recargas aprobadas, logros y
 * movimientos (ADR-04, criterio de aceptación de CP-20).
 */
public record CuadreBilletera(
        int idBilletera,
        int idEstudiante,
        BigDecimal saldoDinero,
        BigDecimal dineroCalculado,
        int saldoMonedas,
        BigDecimal monedasCalculadas) {

    /** {@code true} si ambos saldos coinciden con lo recalculado. */
    public boolean cuadra() {
        return saldoDinero.compareTo(dineroCalculado) == 0
                && monedasCalculadas.compareTo(BigDecimal.valueOf(saldoMonedas)) == 0;
    }
}
