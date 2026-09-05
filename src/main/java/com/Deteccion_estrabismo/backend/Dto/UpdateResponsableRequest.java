package com.Deteccion_estrabismo.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResponsableRequest {
    // Campos heredados de Usuarios
    private String nombres;
    private String apellidos;
    private String correo;
    private String numeroTele;
    
    // Campos específicos de Responsable
    private String parentesco;
    private String ocupacion;
    private String ciudadResidencia;
}
