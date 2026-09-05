package com.Deteccion_estrabismo.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String correo;
    private String password;

    // Getters
    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
