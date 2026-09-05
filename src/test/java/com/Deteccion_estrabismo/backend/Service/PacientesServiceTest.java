package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Entities.Pacientes;
import com.Deteccion_estrabismo.backend.Repository.PacientesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacientesServiceTest {

    @Mock
    private PacientesRepository pacientesRepository;

    @InjectMocks
    private PacientesService pacientesService;

    private Pacientes paciente1;
    private Pacientes paciente2;

    @BeforeEach
    void setUp() {
        paciente1 = new Pacientes();
        paciente1.setId(1L);
        paciente1.setNombres("Paciente Uno");
        
        paciente2 = new Pacientes();
        paciente2.setId(2L);
        paciente2.setNombres("Paciente Dos");
    }

    @Test
    void findAll_ShouldReturnAllPacientes() {
        // Arrange
        List<Pacientes> pacientesList = Arrays.asList(paciente1, paciente2);
        when(pacientesRepository.findAll()).thenReturn(pacientesList);

        // Act
        List<Pacientes> result = pacientesService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(pacientesRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenPacienteExists_ShouldReturnPaciente() {
        // Arrange
        when(pacientesRepository.findById(1L)).thenReturn(Optional.of(paciente1));

        // Act
        Pacientes result = pacientesService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Paciente Uno", result.getNombres());
        verify(pacientesRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenPacienteNotExists_ShouldReturnNull() {
        // Arrange
        when(pacientesRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Pacientes result = pacientesService.findById(99L);

        // Assert
        assertNull(result);
        verify(pacientesRepository, times(1)).findById(99L);
    }

    @Test
    void delete_ShouldCallDeleteById() {
        // Act
        pacientesService.delete(1L);

        // Assert
        verify(pacientesRepository, times(1)).deleteById(1L);
    }

    @Test
    void findByResponsableId_ShouldReturnPacientesForResponsable() {
        // Arrange
        List<Pacientes> pacientesList = Arrays.asList(paciente1);
        when(pacientesRepository.findByResponsableId(1L)).thenReturn(pacientesList);

        // Act
        List<Pacientes> result = pacientesService.findbyResponsableId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(pacientesRepository, times(1)).findByResponsableId(1L);
    }
}
