package com.mycompany.escuelavirtual.model;

import java.math.BigDecimal;

public class Producto {
    private Integer idProducto, precioMonedas, stock = 0;
    private String nombre, descripcion;
    private BigDecimal precioDinero;
    private boolean activo = true;

    public Producto() {
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer v) {
        idProducto = v;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String v) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException("nombre es obligatorio");
        nombre = v;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String v) {
        descripcion = v;
    }

    public BigDecimal getPrecioDinero() {
        return precioDinero;
    }

    public void setPrecioDinero(BigDecimal v) {
        if (v != null && v.signum() < 0)
            throw new IllegalArgumentException("precioDinero inválido");
        precioDinero = v;
    }

    public Integer getPrecioMonedas() {
        return precioMonedas;
    }

    public void setPrecioMonedas(Integer v) {
        if (v != null && v < 0)
            throw new IllegalArgumentException("precioMonedas inválido");
        precioMonedas = v;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer v) {
        if (v == null || v < 0)
            throw new IllegalArgumentException("stock inválido");
        stock = v;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean v) {
        activo = v;
    }

    public void validatePrices() {
        if (precioDinero == null && precioMonedas == null)
            throw new IllegalStateException("Debe existir al menos un precio");
    }
}
