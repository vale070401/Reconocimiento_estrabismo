package com.Deteccion_estrabismo.backend.Dto;

import com.Deteccion_estrabismo.backend.Entities.Rol;
import com.Deteccion_estrabismo.backend.Entities.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterPacienteRequest extends RegisterRequest {
    private String nombres;
    private String apellidos;
    private Integer documentoIdentidad;
    private TipoDocumento tipoDocumento;
    private Date fechaNacimiento;
    private String genero;
    private Integer documentoIdentidadResponsable;

    @Override
    public Rol getRol() {
        return Rol.PACIENTE;
    }

}
