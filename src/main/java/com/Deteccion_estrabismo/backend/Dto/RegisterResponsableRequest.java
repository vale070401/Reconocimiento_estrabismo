package com.Deteccion_estrabismo.backend.Dto;

import com.Deteccion_estrabismo.backend.Entities.Pacientes;
import com.Deteccion_estrabismo.backend.Entities.Rol;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponsableRequest extends RegisterRequest{
    private String parentesco; // "Padre", "Madre", "Tío", "Tutor", etc.
    private String direccion; // dirección del hogar
    private String ciudadResidencia;
    @Override
    public Rol getRol(){
        return Rol.RESPONSABLE;
    }
}

