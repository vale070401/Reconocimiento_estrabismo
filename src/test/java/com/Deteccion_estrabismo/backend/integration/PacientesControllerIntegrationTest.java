package com.Deteccion_estrabismo.backend.integration;

import com.Deteccion_estrabismo.backend.Dto.UpdateRequest;
import com.Deteccion_estrabismo.backend.Entities.*;
import com.Deteccion_estrabismo.backend.Repository.PacientesRepository;
import com.Deteccion_estrabismo.backend.Repository.ResponsableRepository;
import com.Deteccion_estrabismo.backend.Repository.UsuariosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
public class PacientesControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacientesRepository pacientesRepository;

    @MockBean
    private ResponsableRepository responsableRepository;

    @MockBean
    private UsuariosRepository usuariosRepository;

    private Pacientes testPaciente;
    private Responsable testResponsable;

    @BeforeEach
    void setUpTestData() {
        // Configurar datos de prueba
        testResponsable = new Responsable();
        testResponsable.setId(1L);
        testResponsable.setNombres("Responsable");
        testResponsable.setApellidos("Test");
        testResponsable.setCorreo("responsable@test.com");
        testResponsable.setRol(Rol.RESPONSABLE);
        testResponsable.setEnabled(true);

        testPaciente = new Pacientes();
        testPaciente.setId(1L);
        testPaciente.setNombres("Paciente");
        testPaciente.setApellidos("Test");
        // Pacientes doesn't have setCorreo, it's in the parent Usuarios class
        testPaciente.setDocumentoIdentidad(12345678);
        testPaciente.setTipoDocumento(TipoDocumento.CC);
        testPaciente.setFechaNacimiento(new Date());
        testPaciente.setGenero("M");
        testPaciente.setResponsable(testResponsable);

        // Configurar mocks
        when(usuariosRepository.findByCorreo("admin@test.com")).thenReturn(Optional.of(testResponsable));
        when(pacientesRepository.findById(1L)).thenReturn(Optional.of(testPaciente));
        when(pacientesRepository.findAll()).thenReturn(List.of(testPaciente));
        // Remove findByResponsable as it's not defined in the repository
        when(pacientesRepository.save(any(Pacientes.class))).thenReturn(testPaciente);
    }

    @Test
    void whenUpdatePaciente_thenReturnsUpdatedPaciente() throws Exception {
        // Given
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setNombres("Paciente Actualizado");
        updateRequest.setApellidos("Apellido Actualizado");
        updateRequest.setNumeroTele("1234567890");
        updateRequest.setEdad(30);
        updateRequest.setCorreo("paciente.actualizado@test.com");

        // Mock del usuario autenticado
        when(usuariosRepository.findByCorreo(anyString())).thenReturn(Optional.of(testResponsable));
        when(pacientesRepository.findById(anyLong())).thenReturn(Optional.of(testPaciente));
        when(pacientesRepository.save(any(Pacientes.class))).thenReturn(testPaciente);

        // When
        ResultActions response = mockMvc.perform(put("/api/pacientes/Update")
            .header("Authorization", TEST_AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void whenGetPacientesByResponsableId_thenReturnsPacientesList() throws Exception {
        // Given
        Long responsableId = 1L;
        when(pacientesRepository.findByResponsableId(responsableId)).thenReturn(Arrays.asList(testPaciente));

        // When
        ResultActions response = mockMvc.perform(get("/api/pacientes/responsable/" + responsableId)
            .header("Authorization", TEST_AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombres", is(testPaciente.getNombres())));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {})
    void whenUpdatePacienteWithoutAuthentication_thenReturnsError() throws Exception {
        // Given
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setNombres("Paciente No Autorizado");
        updateRequest.setApellidos("Apellido");
        updateRequest.setNumeroTele("1234567890");

        // Mock para simular que no hay un usuario autenticado
        when(usuariosRepository.findByCorreo(anyString())).thenReturn(Optional.empty());

        // When
        ResultActions response = mockMvc.perform(put("/api/pacientes/Update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", is("usuario no encontrado")));
    }
}
