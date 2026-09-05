package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Dto.EvaluacionRequestDto;
import com.Deteccion_estrabismo.backend.Dto.EvaluacionResponseDto;
import com.Deteccion_estrabismo.backend.Entities.Evaluacion;
import com.Deteccion_estrabismo.backend.Entities.Pacientes;
import com.Deteccion_estrabismo.backend.Repository.EvaluacionRepository;
import com.Deteccion_estrabismo.backend.Repository.PacientesRepository;
import com.Deteccion_estrabismo.backend.util.BuildObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final PacientesRepository pacienteRepository;
    private final BuildObjectMapper mapper;

    public EvaluacionResponseDto crearEvaluacion(EvaluacionRequestDto request) {
        // Validaciones básicas
        if (request.getDocumentoIdentidad() == null) {
            throw new RuntimeException("El documento de identidad del paciente es requerido");
        }

        if (request.getConfianzaPrediccion() == null || request.getConfianzaPrediccion() < 0
                || request.getConfianzaPrediccion() > 1) {
            throw new RuntimeException("La confianza debe ser un valor entre 0 y 1");
        }

        // Buscar paciente por documento
        Pacientes paciente = pacienteRepository.findByDocumentoIdentidad(request.getDocumentoIdentidad())
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado con documento: " + request.getDocumentoIdentidad()));

        // Crear evaluación usando el mapper
        Evaluacion evaluacion = mapper.converterTo(request, Evaluacion.class);
        evaluacion.setPaciente(paciente);
        evaluacion.setFechaEvaluacion(LocalDate.parse(request.getFechaEvaluacion()));

        // Actualizar información del paciente
        actualizarInformacionPaciente(paciente, request);

        Evaluacion savedEvaluacion = evaluacionRepository.save(evaluacion);
        return convertirAResponseDTO(savedEvaluacion);
    }

    private void actualizarInformacionPaciente(Pacientes paciente, EvaluacionRequestDto request) {
        Map<String, Object> actualizaciones = Map.of(
                "resultadoDeteccion", request.isResultado() ? "Estrabismo detectado" : "Normal");

        Pacientes pacienteActualizado = mapper.getObjectForUpdate(paciente, actualizaciones);
        pacienteRepository.save(pacienteActualizado);
    }

    public List<EvaluacionResponseDto> obtenerEvaluacionesPorDocumento(Integer documentoIdentidad) {
        return evaluacionRepository.findByPacienteDocumentoIdentidad(documentoIdentidad)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EvaluacionResponseDto> obtenerTodasLasEvaluaciones() {
        return evaluacionRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public EvaluacionResponseDto obtenerEvaluacionPorId(Long id) {
        return evaluacionRepository.findById(id)
                .map(this::convertirAResponseDTO)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con ID: " + id));
    }

    public List<EvaluacionResponseDto> obtenerEvaluacionesPorResultado(boolean resultado) {
        return evaluacionRepository.findByResultado(resultado)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    private EvaluacionResponseDto convertirAResponseDTO(Evaluacion evaluacion) {
        EvaluacionResponseDto dto = mapper.converterTo(evaluacion, EvaluacionResponseDto.class);
        dto.setDocumentoIdentidad(evaluacion.getPaciente().getDocumentoIdentidad());
        dto.setPacienteNombre(evaluacion.getPaciente().getNombres());
        return dto;
    }
}
