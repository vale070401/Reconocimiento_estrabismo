package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Dto.EvaluacionRequestDto;
import com.Deteccion_estrabismo.backend.Dto.EvaluacionResponseDto;
import com.Deteccion_estrabismo.backend.Entities.Evaluacion;
import com.Deteccion_estrabismo.backend.Entities.Pacientes;
import com.Deteccion_estrabismo.backend.Repository.EvaluacionRepository;
import com.Deteccion_estrabismo.backend.Repository.PacientesRepository;
import com.Deteccion_estrabismo.backend.util.BuildObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluacionServiceTest {

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @Mock
    private PacientesRepository pacienteRepository;

    @Mock
    private BuildObjectMapper mapper;

    @InjectMocks
    private EvaluacionService evaluacionService;

    // ========== PRUEBAS CORREGIDAS PARA CREAR EVALUACIÓN ==========

    @Test
    void crearEvaluacion_WhenValidRequest_ShouldCreateEvaluation() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(0.85f);
        request.setResultado(true);
        request.setFechaEvaluacion("2024-01-15");

        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);
        paciente.setNombres("Juan Carlos");

        Evaluacion evaluacion = new Evaluacion();
        Evaluacion savedEvaluacion = new Evaluacion();
        savedEvaluacion.setId(1L);
        savedEvaluacion.setPaciente(paciente);

        //  CREAR DTO MANUALMENTE en lugar de depender del mapper
        EvaluacionResponseDto expectedResponse = EvaluacionResponseDto.builder()
                .id(1L)
                .documentoIdentidad(123456789)
                .pacienteNombre("Juan Carlos")
                .build();

        when(pacienteRepository.findByDocumentoIdentidad(123456789))
                .thenReturn(Optional.of(paciente));
        when(mapper.converterTo(request, Evaluacion.class)).thenReturn(evaluacion);
        when(evaluacionRepository.save(evaluacion)).thenReturn(savedEvaluacion);

        // El mapper puede retornar null, así que creamos el DTO manualmente
        when(mapper.converterTo(savedEvaluacion, EvaluacionResponseDto.class))
                .thenReturn(expectedResponse);

        // Act
        EvaluacionResponseDto response = evaluacionService.crearEvaluacion(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(123456789, response.getDocumentoIdentidad());
        assertEquals("Juan Carlos", response.getPacienteNombre());
    }

    @Test
    void crearEvaluacion_WhenDocumentoIdentidadNull_ShouldThrowException() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(null);
        request.setConfianzaPrediccion(0.85f);
        request.setFechaEvaluacion("2024-01-15");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluacionService.crearEvaluacion(request));

        assertEquals("El documento de identidad del paciente es requerido", exception.getMessage());
        verify(pacienteRepository, never()).findByDocumentoIdentidad(anyInt());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void crearEvaluacion_WhenConfianzaPrediccionNull_ShouldThrowException() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(null);
        request.setFechaEvaluacion("2024-01-15");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluacionService.crearEvaluacion(request));

        assertEquals("La confianza debe ser un valor entre 0 y 1", exception.getMessage());
    }

    @Test
    void crearEvaluacion_WhenConfianzaPrediccionLessThanZero_ShouldThrowException() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(-0.1f);
        request.setFechaEvaluacion("2024-01-15");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluacionService.crearEvaluacion(request));

        assertEquals("La confianza debe ser un valor entre 0 y 1", exception.getMessage());
    }

    @Test
    void crearEvaluacion_WhenConfianzaPrediccionGreaterThanOne_ShouldThrowException() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(1.1f);
        request.setFechaEvaluacion("2024-01-15");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluacionService.crearEvaluacion(request));

        assertEquals("La confianza debe ser un valor entre 0 y 1", exception.getMessage());
    }

    @Test
    void crearEvaluacion_WhenConfianzaPrediccionZero_ShouldBeValid() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(0.0f);
        request.setResultado(false);
        request.setFechaEvaluacion("2024-01-15");

        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);

        Evaluacion evaluacion = new Evaluacion();
        Evaluacion savedEvaluacion = new Evaluacion();
        savedEvaluacion.setId(1L);
        savedEvaluacion.setPaciente(paciente);

        EvaluacionResponseDto expectedResponse = EvaluacionResponseDto.builder()
                .id(1L)
                .documentoIdentidad(123456789)
                .pacienteNombre("Test Paciente")
                .build();

        when(pacienteRepository.findByDocumentoIdentidad(123456789))
                .thenReturn(Optional.of(paciente));
        when(mapper.converterTo(request, Evaluacion.class)).thenReturn(evaluacion);
        when(evaluacionRepository.save(evaluacion)).thenReturn(savedEvaluacion);
        when(mapper.getObjectForUpdate(any(Pacientes.class), anyMap())).thenReturn(paciente);
        when(mapper.converterTo(savedEvaluacion, EvaluacionResponseDto.class))
                .thenReturn(expectedResponse);

        // Act
        EvaluacionResponseDto response = evaluacionService.crearEvaluacion(request);

        // Assert
        assertNotNull(response);
        // No debe lanzar excepción para confianza = 0.0f
    }

    @Test
    void crearEvaluacion_WhenConfianzaPrediccionOne_ShouldBeValid() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(1.0f);
        request.setResultado(true);
        request.setFechaEvaluacion("2024-01-15");

        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);

        Evaluacion evaluacion = new Evaluacion();
        Evaluacion savedEvaluacion = new Evaluacion();
        savedEvaluacion.setId(1L);
        savedEvaluacion.setPaciente(paciente);

        EvaluacionResponseDto expectedResponse = EvaluacionResponseDto.builder()
                .id(1L)
                .documentoIdentidad(123456789)
                .pacienteNombre("Test Paciente")
                .build();

        when(pacienteRepository.findByDocumentoIdentidad(123456789))
                .thenReturn(Optional.of(paciente));
        when(mapper.converterTo(request, Evaluacion.class)).thenReturn(evaluacion);
        when(evaluacionRepository.save(evaluacion)).thenReturn(savedEvaluacion);
        when(mapper.getObjectForUpdate(any(Pacientes.class), anyMap())).thenReturn(paciente);
        when(mapper.converterTo(savedEvaluacion, EvaluacionResponseDto.class))
                .thenReturn(expectedResponse);

        // Act
        EvaluacionResponseDto response = evaluacionService.crearEvaluacion(request);

        // Assert
        assertNotNull(response);
        // No debe lanzar excepción para confianza = 1.0f
    }

    @Test
    void crearEvaluacion_WhenPacienteNotFound_ShouldThrowException() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(999999999);
        request.setConfianzaPrediccion(0.85f);
        request.setFechaEvaluacion("2024-01-15");

        when(pacienteRepository.findByDocumentoIdentidad(999999999))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluacionService.crearEvaluacion(request));

        assertEquals("Paciente no encontrado con documento: 999999999", exception.getMessage());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void crearEvaluacion_WhenResultadoTrue_ShouldUpdatePacienteWithEstrabismo() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(0.95f);
        request.setResultado(true);
        request.setFechaEvaluacion("2024-01-15");

        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);
        paciente.setNombres("Maria Garcia");

        Evaluacion evaluacion = new Evaluacion();
        Evaluacion savedEvaluacion = new Evaluacion();
        savedEvaluacion.setId(1L);
        savedEvaluacion.setPaciente(paciente);

        Pacientes pacienteActualizado = new Pacientes();
        pacienteActualizado.setDocumentoIdentidad(123456789);

        EvaluacionResponseDto expectedResponse = EvaluacionResponseDto.builder()
                .id(1L)
                .documentoIdentidad(123456789)
                .pacienteNombre("Maria Garcia")
                .build();

        when(pacienteRepository.findByDocumentoIdentidad(123456789))
                .thenReturn(Optional.of(paciente));
        when(mapper.converterTo(request, Evaluacion.class)).thenReturn(evaluacion);
        when(evaluacionRepository.save(evaluacion)).thenReturn(savedEvaluacion);
        when(mapper.getObjectForUpdate(any(Pacientes.class), argThat(map ->
                map.get("resultadoDeteccion").equals("Estrabismo detectado")
        ))).thenReturn(pacienteActualizado);
        when(mapper.converterTo(savedEvaluacion, EvaluacionResponseDto.class))
                .thenReturn(expectedResponse);

        // Act
        EvaluacionResponseDto response = evaluacionService.crearEvaluacion(request);

        // Assert
        assertNotNull(response);
        verify(mapper).getObjectForUpdate(any(Pacientes.class), argThat(map ->
                map.get("resultadoDeteccion").equals("Estrabismo detectado")
        ));
        verify(pacienteRepository).save(pacienteActualizado);
    }

    @Test
    void crearEvaluacion_WhenResultadoFalse_ShouldUpdatePacienteWithNormal() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(0.15f);
        request.setResultado(false);
        request.setFechaEvaluacion("2024-01-15");

        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);

        Evaluacion evaluacion = new Evaluacion();
        Evaluacion savedEvaluacion = new Evaluacion();
        savedEvaluacion.setId(1L);
        savedEvaluacion.setPaciente(paciente);

        EvaluacionResponseDto expectedResponse = EvaluacionResponseDto.builder()
                .id(1L)
                .documentoIdentidad(123456789)
                .pacienteNombre("Test Paciente")
                .build();

        when(pacienteRepository.findByDocumentoIdentidad(123456789))
                .thenReturn(Optional.of(paciente));
        when(mapper.converterTo(request, Evaluacion.class)).thenReturn(evaluacion);
        when(evaluacionRepository.save(evaluacion)).thenReturn(savedEvaluacion);
        when(mapper.getObjectForUpdate(any(Pacientes.class), argThat(map ->
                map.get("resultadoDeteccion").equals("Normal")
        ))).thenReturn(paciente);
        when(mapper.converterTo(savedEvaluacion, EvaluacionResponseDto.class))
                .thenReturn(expectedResponse);

        // Act
        EvaluacionResponseDto response = evaluacionService.crearEvaluacion(request);

        // Assert
        assertNotNull(response);
        verify(mapper).getObjectForUpdate(any(Pacientes.class), argThat(map ->
                map.get("resultadoDeteccion").equals("Normal")
        ));
    }

    @Test
    void crearEvaluacion_ShouldParseFechaEvaluacionCorrectly() {
        // Arrange
        EvaluacionRequestDto request = new EvaluacionRequestDto();
        request.setDocumentoIdentidad(123456789);
        request.setConfianzaPrediccion(0.85f);
        request.setResultado(true);
        request.setFechaEvaluacion("2024-12-25");

        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);

        Evaluacion evaluacionMock = mock(Evaluacion.class);
        Evaluacion savedEvaluacion = new Evaluacion();
        savedEvaluacion.setId(1L);
        savedEvaluacion.setPaciente(paciente);

        EvaluacionResponseDto expectedResponse = EvaluacionResponseDto.builder()
                .id(1L)
                .documentoIdentidad(123456789)
                .pacienteNombre("Test Paciente")
                .build();

        when(pacienteRepository.findByDocumentoIdentidad(123456789))
                .thenReturn(Optional.of(paciente));
        when(mapper.converterTo(request, Evaluacion.class)).thenReturn(evaluacionMock);
        when(evaluacionRepository.save(evaluacionMock)).thenReturn(savedEvaluacion);
        when(mapper.getObjectForUpdate(any(Pacientes.class), anyMap())).thenReturn(paciente);
        when(mapper.converterTo(savedEvaluacion, EvaluacionResponseDto.class))
                .thenReturn(expectedResponse);

        // Act
        EvaluacionResponseDto response = evaluacionService.crearEvaluacion(request);

        // Assert
        assertNotNull(response);
        verify(evaluacionMock).setFechaEvaluacion(LocalDate.parse("2024-12-25"));
    }
    // ========== PRUEBAS  PARA OBTENER EVALUACIONES ==========

    @Test
    void obtenerEvaluacionesPorDocumento_WhenEvaluationsExist_ShouldReturnList() {
        // Arrange
        Integer documentoIdentidad = 123456789;
        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(documentoIdentidad);
        paciente.setNombres("Carlos Lopez");

        Evaluacion evaluacion1 = new Evaluacion();
        evaluacion1.setId(1L);
        evaluacion1.setPaciente(paciente);

        Evaluacion evaluacion2 = new Evaluacion();
        evaluacion2.setId(2L);
        evaluacion2.setPaciente(paciente);

        List<Evaluacion> evaluaciones = List.of(evaluacion1, evaluacion2);


        EvaluacionResponseDto dto1 = EvaluacionResponseDto.builder()
                .id(1L)
                .documentoIdentidad(documentoIdentidad)
                .pacienteNombre("Carlos Lopez")
                .build();

        EvaluacionResponseDto dto2 = EvaluacionResponseDto.builder()
                .id(2L)
                .documentoIdentidad(documentoIdentidad)
                .pacienteNombre("Carlos Lopez")
                .build();

        when(evaluacionRepository.findByPacienteDocumentoIdentidad(documentoIdentidad))
                .thenReturn(evaluaciones);
        when(mapper.converterTo(evaluacion1, EvaluacionResponseDto.class)).thenReturn(dto1);
        when(mapper.converterTo(evaluacion2, EvaluacionResponseDto.class)).thenReturn(dto2);

        // Act
        List<EvaluacionResponseDto> result = evaluacionService.obtenerEvaluacionesPorDocumento(documentoIdentidad);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void obtenerEvaluacionesPorDocumento_WhenNoEvaluations_ShouldReturnEmptyList() {
        // Arrange
        Integer documentoIdentidad = 999999999;
        when(evaluacionRepository.findByPacienteDocumentoIdentidad(documentoIdentidad))
                .thenReturn(List.of());

        // Act
        List<EvaluacionResponseDto> result = evaluacionService.obtenerEvaluacionesPorDocumento(documentoIdentidad);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerTodasLasEvaluaciones_ShouldReturnAllEvaluations() {
        // Arrange
        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);
        paciente.setNombres("Test Paciente");

        Evaluacion evaluacion1 = new Evaluacion();
        evaluacion1.setId(1L);
        evaluacion1.setPaciente(paciente);

        Evaluacion evaluacion2 = new Evaluacion();
        evaluacion2.setId(2L);
        evaluacion2.setPaciente(paciente);

        List<Evaluacion> evaluaciones = List.of(evaluacion1, evaluacion2);


        EvaluacionResponseDto dto1 = EvaluacionResponseDto.builder().id(1L).build();
        EvaluacionResponseDto dto2 = EvaluacionResponseDto.builder().id(2L).build();

        when(evaluacionRepository.findAll()).thenReturn(evaluaciones);
        when(mapper.converterTo(evaluacion1, EvaluacionResponseDto.class)).thenReturn(dto1);
        when(mapper.converterTo(evaluacion2, EvaluacionResponseDto.class)).thenReturn(dto2);

        // Act
        List<EvaluacionResponseDto> result = evaluacionService.obtenerTodasLasEvaluaciones();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void obtenerEvaluacionPorId_WhenExists_ShouldReturnEvaluation() {
        // Arrange
        Long evaluacionId = 1L;
        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);
        paciente.setNombres("Ana Martinez");

        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setId(evaluacionId);
        evaluacion.setPaciente(paciente);


        EvaluacionResponseDto expectedDto = EvaluacionResponseDto.builder()
                .id(evaluacionId)
                .documentoIdentidad(123456789)
                .pacienteNombre("Ana Martinez")
                .build();

        when(evaluacionRepository.findById(evaluacionId)).thenReturn(Optional.of(evaluacion));
        when(mapper.converterTo(evaluacion, EvaluacionResponseDto.class)).thenReturn(expectedDto);

        // Act
        EvaluacionResponseDto result = evaluacionService.obtenerEvaluacionPorId(evaluacionId);

        // Assert
        assertNotNull(result);
        assertEquals(evaluacionId, result.getId());
        assertEquals(123456789, result.getDocumentoIdentidad());
        assertEquals("Ana Martinez", result.getPacienteNombre());
    }

    @Test
    void obtenerEvaluacionPorId_WhenNotExists_ShouldThrowException() {
        // Arrange
        Long evaluacionId = 999L;
        when(evaluacionRepository.findById(evaluacionId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluacionService.obtenerEvaluacionPorId(evaluacionId));

        assertEquals("Evaluación no encontrada con ID: 999", exception.getMessage());
    }

    @Test
    void obtenerEvaluacionesPorResultado_WhenTrue_ShouldReturnPositiveEvaluations() {
        // Arrange
        boolean resultado = true;
        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);
        paciente.setNombres("Test Paciente");

        Evaluacion evaluacion1 = new Evaluacion();
        evaluacion1.setId(1L);
        evaluacion1.setResultado(true);
        evaluacion1.setPaciente(paciente);

        Evaluacion evaluacion2 = new Evaluacion();
        evaluacion2.setId(2L);
        evaluacion2.setResultado(true);
        evaluacion2.setPaciente(paciente);

        List<Evaluacion> evaluaciones = List.of(evaluacion1, evaluacion2);


        EvaluacionResponseDto dto1 = EvaluacionResponseDto.builder().id(1L).build();
        EvaluacionResponseDto dto2 = EvaluacionResponseDto.builder().id(2L).build();

        when(evaluacionRepository.findByResultado(resultado)).thenReturn(evaluaciones);
        when(mapper.converterTo(evaluacion1, EvaluacionResponseDto.class)).thenReturn(dto1);
        when(mapper.converterTo(evaluacion2, EvaluacionResponseDto.class)).thenReturn(dto2);

        // Act
        List<EvaluacionResponseDto> result = evaluacionService.obtenerEvaluacionesPorResultado(resultado);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void obtenerEvaluacionesPorResultado_WhenFalse_ShouldReturnNegativeEvaluations() {
        // Arrange
        boolean resultado = false;
        Pacientes paciente = new Pacientes();
        paciente.setDocumentoIdentidad(123456789);
        paciente.setNombres("Test Paciente");

        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setId(1L);
        evaluacion.setResultado(false);
        evaluacion.setPaciente(paciente);

        List<Evaluacion> evaluaciones = List.of(evaluacion);


        EvaluacionResponseDto dto = EvaluacionResponseDto.builder().id(1L).build();

        when(evaluacionRepository.findByResultado(resultado)).thenReturn(evaluaciones);
        when(mapper.converterTo(evaluacion, EvaluacionResponseDto.class)).thenReturn(dto);

        // Act
        List<EvaluacionResponseDto> result = evaluacionService.obtenerEvaluacionesPorResultado(resultado);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}