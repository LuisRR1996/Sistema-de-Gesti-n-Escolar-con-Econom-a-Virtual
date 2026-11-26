package com.mycompany.escuelavirtual.model;

import java.math.BigDecimal;

public class Billetera {
    private Integer idBilletera, idEstudiante, saldoMonedas = 0;
    private BigDecimal saldoDinero = BigDecimal.ZERO;

    public Billetera() {
    }

    public Billetera(Integer idBilletera, Integer idEstudiante, BigDecimal saldoDinero, Integer saldoMonedas) {
        this.idBilletera = idBilletera;
        setIdEstudiante(idEstudiante);
        setSaldoDinero(saldoDinero);
        setSaldoMonedas(saldoMonedas);
    }

    public Integer getIdBilletera() {
        return idBilletera;
    }

    public void setIdBilletera(Integer v) {
        idBilletera = v;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer v) {
        idEstudiante = positive(v, "idEstudiante");
    }

    public BigDecimal getSaldoDinero() {
        return saldoDinero;
    }

    public void setSaldoDinero(BigDecimal v) {
        if (v == null || v.signum() < 0)
            throw new IllegalArgumentException("saldoDinero inválido");
        saldoDinero = v;
    }

    public Integer getSaldoMonedas() {
        return saldoMonedas;
    }

    public void setSaldoMonedas(Integer v) {
        saldoMonedas = nonNegative(v, "saldoMonedas");
    }

    private static Integer positive(Integer v, String n) {
        if (v == null || v < 1)
            throw new IllegalArgumentException(n + " debe ser positivo");
        return v;
    }

    private static Integer nonNegative(Integer v, String n) {
        if (v == null || v < 0)
            throw new IllegalArgumentException(n + " no puede ser negativo");
        return v;
    }
}
