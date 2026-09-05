package com.Deteccion_estrabismo.backend.Dto;

import java.util.Date;

import com.Deteccion_estrabismo.backend.Entities.Rol;
import com.Deteccion_estrabismo.backend.Entities.TipoDocumento;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class RegisterRequest {

    private String nombres;
    private String apellidos;
    private Integer documentoIdentidad;
    private String correo;
    private String password; // se cifra con Bcrypt
    private String numeroTele;
    private TipoDocumento tipoDocumento;
    private Date fechaNacimiento;

    public abstract Rol getRol();

}
