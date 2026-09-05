package com.Deteccion_estrabismo.backend.integration;

import com.Deteccion_estrabismo.backend.Dto.LoginRequest;
import com.Deteccion_estrabismo.backend.Dto.RegisterPacienteRequest;
import com.Deteccion_estrabismo.backend.Entities.Rol;
import com.Deteccion_estrabismo.backend.Entities.TipoDocumento;
import com.Deteccion_estrabismo.backend.Entities.Usuarios;
import com.Deteccion_estrabismo.backend.Repository.UsuariosRepository;
import com.Deteccion_estrabismo.backend.Service.JwtService;
import org.springframework.security.test.context.support.WithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BASE_URL = "/auth";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Mock user for login
        Usuarios mockUser = new Usuarios();
        mockUser.setCorreo(TEST_EMAIL);
        mockUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        mockUser.setRol(Rol.ADMIN);
        mockUser.setEnabled(true);

        when(usuariosRepository.findByCorreo(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(any())).thenReturn("mocked.jwt.token");
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    void whenValidLogin_thenReturnsJwtToken() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
            .correo(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

        // When
        ResultActions response = mockMvc.perform(post(BASE_URL + "/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void whenRegisterNewUser_thenReturnsSuccess() throws Exception {
        // Given
        RegisterPacienteRequest registerRequest = RegisterPacienteRequest.builder()
            .nombres("Test")
            .apellidos("User")
            .documentoIdentidad(12345678)
            .tipoDocumento(TipoDocumento.CC)
            .fechaNacimiento(new Date())
            .genero("M")
            .documentoIdentidadResponsable(87654321)
            .build();

        // When
        ResultActions response = mockMvc.perform(post(BASE_URL + "/register/paciente")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void whenHealthCheck_thenReturnsOk() throws Exception {
        // When
        ResultActions response = mockMvc.perform(get(BASE_URL + "/health"));

        // Then
        response.andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void whenRegisterPaciente_thenReturnsSuccess() throws Exception {
        // Given
        RegisterPacienteRequest request = RegisterPacienteRequest.builder()
            .nombres("Paciente")
            .apellidos("Test")
            .documentoIdentidad(12345678)
            .tipoDocumento(TipoDocumento.CC)
            .fechaNacimiento(new Date())
            .genero("M")
            .documentoIdentidadResponsable(87654321)
            .build();

        // Mock the save operation
        when(usuariosRepository.save(any(Usuarios.class))).thenAnswer(invocation -> {
            Usuarios user = invocation.getArgument(0);
            user.setId(1L); // Set an ID to simulate saved entity
            return user;
        });

        // When
        ResultActions response = mockMvc.perform(post(BASE_URL + "/register/paciente")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.error").exists());
    }
}
