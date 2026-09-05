package com.Deteccion_estrabismo.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionResponseDto {
    private Long id;
    private Integer documentoIdentidad;
    private String pacienteNombre;
    private boolean resultado;
    private Float confianzaPrediccion;
    private LocalDate fechaEvaluacion;

    public Float getConfianzaPrediccion() {
        return confianzaPrediccion;
    }

    public void setConfianzaPrediccion(Float confianzaPrediccion) {
        this.confianzaPrediccion = confianzaPrediccion;
    }

    public LocalDate getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(LocalDate fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public boolean isResultado() {
        return resultado;
    }

    public void setResultado(boolean resultado) {
        this.resultado = resultado;
    }

    public Integer getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(Integer documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }
}
