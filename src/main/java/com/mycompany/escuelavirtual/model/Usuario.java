package com.mycompany.escuelavirtual.model;

import com.mycompany.escuelavirtual.model.enums.Rol;

import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario {
    private Integer idUsuario, idPadre;
    private String nombres, apellidos, correo, passwordHash, grado, seccion;
    private Rol rol;
    private boolean estado = true;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta, ultimoAcceso;

    public Usuario() {

    }

    public Usuario(Integer idUsuario, String nombres, String apellidos, String correo, String passwordHash,
            Rol rol, String grado, String seccion, Integer idPadre, boolean estado) {
        this.idUsuario = idUsuario;
        setNombres(nombres);
        setApellidos(apellidos);
        setCorreo(correo);
        setPasswordHash(passwordHash);
        setRol(rol);
        this.grado = grado;
        this.seccion = seccion;
        this.idPadre = idPadre;
        this.estado = estado;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer v) {
        idUsuario = v;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String v) {
        nombres = required(v, "nombres");
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String v) {
        apellidos = required(v, "apellidos");
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String v) {
        correo = required(v, "correo");
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String v) {
        passwordHash = required(v, "passwordHash");
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol v) {
        rol = java.util.Objects.requireNonNull(v, "rol");
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String v) {
        grado = v;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String v) {
        seccion = v;
    }

    public Integer getIdPadre() {
        return idPadre;
    }

    public void setIdPadre(Integer v) {
        idPadre = v;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean v) {
        estado = v;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int v) {
        if (v < 0)
            throw new IllegalArgumentException("intentosFallidos no puede ser negativo");
        intentosFallidos = v;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime v) {
        bloqueadoHasta = v;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime v) {
        ultimoAcceso = v;
    }

    /** Indica si la cuenta sigue bloqueada en el instante indicado. */
    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(Objects.requireNonNull(ahora, "ahora"));
    }

    /** Nombre para mostrar en la cabecera y en los saludos. */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    private static String required(String v, String name) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException(name + " es obligatorio");
        return v;
    }
}
