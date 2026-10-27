package com.mycompany.escuelavirtual.model;

import com.mycompany.escuelavirtual.model.enums.Rol;

public class Usuario {
    private Integer idUsuario, idPadre;
    private String nombres, apellidos, correo, passwordHash, grado, seccion;
    private Rol rol;
    private boolean estado = true;

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

    private static String required(String v, String name) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException(name + " es obligatorio");
        return v;
    }
}
