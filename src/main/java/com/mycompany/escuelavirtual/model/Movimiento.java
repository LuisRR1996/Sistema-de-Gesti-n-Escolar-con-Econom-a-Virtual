package com.mycompany.escuelavirtual.model;

import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Movimiento {
    private Integer idMovimiento, idBilletera, idProducto, cantidad;
    private TipoMoneda metodoPago;
    private BigDecimal montoTotal;
    private LocalDateTime fecha;

    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer v) {
        idMovimiento = v;
    }

    public Integer getIdBilletera() {
        return idBilletera;
    }

    public void setIdBilletera(Integer v) {
        idBilletera = v;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer v) {
        idProducto = v;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer v) {
        if (v == null || v < 1)
            throw new IllegalArgumentException("cantidad debe ser positiva");
        cantidad = v;
    }

    public TipoMoneda getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(TipoMoneda v) {
        metodoPago = v;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal v) {
        if (v == null || v.signum() <= 0)
            throw new IllegalArgumentException("montoTotal debe ser positivo");
        montoTotal = v;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime v) {
        fecha = v;
    }
}
