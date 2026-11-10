package com.mycompany.escuelavirtual.model;

import java.time.LocalDateTime;

public class Logro {
    private Integer idLogro, idEstudiante, idDocente, monedas;
    private String tipo, descripcion;
    private LocalDateTime fecha;

    public Integer getIdLogro() {
        return idLogro;
    }

    public void setIdLogro(Integer v) {
        idLogro = v;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer v) {
        idEstudiante = positive(v, "idEstudiante");
    }

    public Integer getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Integer v) {
        idDocente = positive(v, "idDocente");
    }

    public Integer getMonedas() {
        return monedas;
    }

    public void setMonedas(Integer v) {
        monedas = positive(v, "monedas");
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String v) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException("tipo es obligatorio");
        tipo = v;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String v) {
        descripcion = v;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime v) {
        fecha = v;
    }

    private static Integer positive(Integer v, String n) {
        if (v == null || v < 1)
            throw new IllegalArgumentException(n + " debe ser positivo");
        return v;
    }
}
