package com.Deteccion_estrabismo.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionRequestDto {
    private Integer documentoIdentidad;
    private boolean resultado;
    private Float confianzaPrediccion;
    private String fechaEvaluacion;

    public boolean isResultado() {
        return resultado;
    }

    public void setResultado(boolean resultado) {
        this.resultado = resultado;
    }

    public String getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(String fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public Float getConfianzaPrediccion() {
        return confianzaPrediccion;
    }

    public void setConfianzaPrediccion(Float confianzaPrediccion) {
        this.confianzaPrediccion = confianzaPrediccion;
    }

    public Integer getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(Integer documentoPaciente) {
        this.documentoIdentidad = documentoPaciente;
    }
}
