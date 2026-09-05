package com.Deteccion_estrabismo.backend.Dto;

import lombok.Data;

@Data
public class UpdateRequest {
    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getNumeroTele() {
        return numeroTele;
    }

    public void setNumeroTele(String numeroTele) {
        this.numeroTele = numeroTele;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    private String nombres;
    private String apellidos;
    private Integer edad;
    private String correo;
    private String numeroTele;
}
