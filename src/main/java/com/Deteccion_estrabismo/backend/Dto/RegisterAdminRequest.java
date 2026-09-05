package com.Deteccion_estrabismo.backend.Dto;

import com.Deteccion_estrabismo.backend.Entities.Rol;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RegisterAdminRequest extends RegisterRequest{
    @NotBlank(message = "El cargo es requerido")
    private String cargo;

    private String permisos;
    private String areaResponsable;

    @Override
    public Rol getRol() {
        return Rol.ADMIN;
    }
}
