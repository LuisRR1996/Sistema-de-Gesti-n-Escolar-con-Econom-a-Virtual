package com.mycompany.escuelavirtual.model;

import com.mycompany.escuelavirtual.model.enums.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Recarga {
    private Integer idRecarga, idBilletera, idPadre;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoRecarga estado;
    private LocalDateTime fecha;

    public Integer getIdRecarga() {
        return idRecarga;
    }

    public void setIdRecarga(Integer v) {
        idRecarga = v;
    }

    public Integer getIdBilletera() {
        return idBilletera;
    }

    public void setIdBilletera(Integer v) {
        idBilletera = v;
    }

    public Integer getIdPadre() {
        return idPadre;
    }

    public void setIdPadre(Integer v) {
        idPadre = v;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal v) {
        if (v == null || v.signum() <= 0)
            throw new IllegalArgumentException("monto debe ser positivo");
        monto = v;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago v) {
        metodoPago = v;
    }

    public EstadoRecarga getEstado() {
        return estado;
    }

    public void setEstado(EstadoRecarga v) {
        estado = v;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime v) {
        fecha = v;
    }
}
