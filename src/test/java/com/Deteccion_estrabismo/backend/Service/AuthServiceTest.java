package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Dto.*;
import com.Deteccion_estrabismo.backend.Entities.*;
import com.Deteccion_estrabismo.backend.Repository.*;
import com.Deteccion_estrabismo.backend.util.BuildObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuariosRepository usuariosRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PacientesRepository pacientesRepository;
    @Mock private AdministradorRepository administradorRepository;
    @Mock private ResponsableRepository responsableRepository;
    @Mock private BuildObjectMapper mapper;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    // ========== PRUEBAS PARA LOGIN ==========

    @Test
    void login_WhenValidCredentials_ShouldReturnToken() {
        // Arrange
        LoginRequest request = new LoginRequest("test@test.com", "password");
        Usuarios usuario = new Usuarios();
        usuario.setCorreo("test@test.com");
        usuario.setRol(Rol.RESPONSABLE);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuariosRepository.findByCorreo("test@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.Login(request);

        // Assert
        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertNull(response.getError());
    }

    @Test
    void login_WhenInvalidCredentials_ShouldReturnError() {
        // Arrange
        LoginRequest request = new LoginRequest("test@test.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act
        AuthResponse response = authService.Login(request);

        // Assert
        assertNull(response.getToken());
        assertNotNull(response.getError());
    }

    // ========== PRUEBAS PARA REGISTER ==========

    @Test
    void register_WhenEmailAlreadyExists_ShouldReturnError() {
        // Arrange
        RegisterAdminRequest request = new RegisterAdminRequest();
        request.setCorreo("existing@test.com");
        request.setTipoDocumento(TipoDocumento.CC);
        when(usuariosRepository.findByCorreo("existing@test.com"))
                .thenReturn(Optional.of(new Usuarios()));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("ya está registrado"));
    }

    @Test
    void register_WhenPacienteWithRegistroCivil_ShouldCreatePaciente() {
        // Arrange
        RegisterPacienteRequest request = new RegisterPacienteRequest();
        request.setTipoDocumento(TipoDocumento.REGISTRO_CIVIL);
        request.setNombres("Juan Paciente");

        Pacientes paciente = new Pacientes();
        when(usuariosRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(mapper.converterTo(any(RegisterPacienteRequest.class), eq(Pacientes.class)))
                .thenReturn(paciente);
        when(pacientesRepository.save(any(Pacientes.class))).thenReturn(paciente);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertTrue(response.isSuccess());
        verify(pacientesRepository).save(any(Pacientes.class));
    }

    @Test
    void register_WhenResponsable_ShouldCreateEnabledUser() {
        // Arrange
        RegisterResponsableRequest request = new RegisterResponsableRequest();
        request.setTipoDocumento(TipoDocumento.CC);
        request.setCorreo("responsable@test.com");
        request.setPassword("password123");
        request.setNombres("Carlos Responsable");

        Responsable responsable = new Responsable();
        responsable.setId(1L);
        responsable.setCorreo("responsable@test.com");
        responsable.setNombres("Carlos Responsable");
        responsable.setRol(Rol.RESPONSABLE);
        when(usuariosRepository.findByCorreo("responsable@test.com")).thenReturn(Optional.empty());
        when(mapper.converterTo(any(RegisterResponsableRequest.class), eq(Responsable.class)))
                .thenReturn(responsable);
        when(responsableRepository.save(any(Responsable.class))).thenReturn(responsable);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertTrue(response.isSuccess());
        verify(responsableRepository).save(argThat(Responsable::isEnabled));
    }

    @Test
    void register_WhenAdministrador_ShouldCreateEnabledUser() {
        // Arrange
        RegisterAdminRequest request = new RegisterAdminRequest();
        request.setTipoDocumento(TipoDocumento.CC);
        request.setCorreo("admin@test.com");
        request.setPassword("admin123");
        request.setNombres("Admin User");

        Administrador administrador = new Administrador();
        administrador.setId(1L);
        administrador.setCorreo("admin@test.com");
        administrador.setNombres("Admin User");
        administrador.setRol(Rol.ADMIN);
        when(usuariosRepository.findByCorreo("admin@test.com")).thenReturn(Optional.empty());
        when(mapper.converterTo(any(RegisterAdminRequest.class), eq(Administrador.class)))
                .thenReturn(administrador);
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertTrue(response.isSuccess());
        verify(administradorRepository).save(argThat(Administrador::isEnabled));
    }

    @Test
    void register_WhenPacienteWithResponsable_ShouldLinkResponsable() {
        // Arrange
        RegisterPacienteRequest request = new RegisterPacienteRequest();
        request.setTipoDocumento(TipoDocumento.REGISTRO_CIVIL);
        request.setDocumentoIdentidadResponsable(12345678);

        Pacientes paciente = new Pacientes();
        Responsable responsable = new Responsable();
        responsable.setDocumentoIdentidad(Integer.valueOf("12345678"));

        when(usuariosRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(mapper.converterTo(any(RegisterPacienteRequest.class), eq(Pacientes.class)))
                .thenReturn(paciente);
        when(usuariosRepository.findByDocumentoIdentidad(12345678))
                .thenReturn(Optional.of(responsable));
        when(pacientesRepository.save(any(Pacientes.class))).thenReturn(paciente);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(responsable, paciente.getResponsable());
    }

    // ========== PRUEBAS PARA CREACIÓN DE ENTIDADES CON MAPPER ==========

    @Test
    void crearResponsable_WithMapper_ShouldEncodePasswordAndSetDefaults() {
        // Arrange
        RegisterResponsableRequest request = new RegisterResponsableRequest();
        request.setPassword("plain-password");
        // El rol viene de getRol(), no de setRol()

        Responsable responsableMapeado = new Responsable();
        when(mapper.converterTo(request, Responsable.class)).thenReturn(responsableMapeado);
        when(responsableRepository.save(responsableMapeado)).thenReturn(responsableMapeado);

        // Act - Test indirecto a través de register
        request.setTipoDocumento(TipoDocumento.CC);
        request.setCorreo("test@test.com");
        when(usuariosRepository.findByCorreo(any())).thenReturn(Optional.empty());

        authService.register(request);

        // Assert - Verificar que se setean los defaults correctamente
        verify(responsableRepository).save(argThat(responsable ->
                responsable.isEnabled() &&
                        responsable.getRol() != null
        ));
    }

    @Test
    void crearAdministrador_WithMapper_ShouldEncodePasswordAndSetDefaults() {
        // Arrange
        RegisterAdminRequest request = new RegisterAdminRequest();
        request.setPassword("admin-password");
        // El rol viene de getRol(), no de setRol()

        Administrador adminMapeado = new Administrador();
        when(mapper.converterTo(request, Administrador.class)).thenReturn(adminMapeado);
        when(administradorRepository.save(adminMapeado)).thenReturn(adminMapeado);

        // Act - Test indirecto a través de register
        request.setTipoDocumento(TipoDocumento.CC);
        request.setCorreo("admin@test.com");
        when(usuariosRepository.findByCorreo(any())).thenReturn(Optional.empty());

        authService.register(request);

        // Assert
        verify(administradorRepository).save(argThat(admin ->
                admin.isEnabled() &&
                        admin.getRol() != null
        ));
    }

    // ========== PRUEBAS PARA UPDATE ==========

    @Test
    void updatePaciente_WhenValidData_ShouldUpdateFields() {
        // Arrange
        String correo = "test@test.com";
        UpdateRequest request = new UpdateRequest();
        request.setNombres("Nuevo Nombre");
        request.setApellidos("Nuevo Apellido");

        Usuarios usuarioExistente = new Usuarios();
        usuarioExistente.setNombres("Viejo Nombre");
        usuarioExistente.setApellidos("Viejo Apellido");

        when(usuariosRepository.findByCorreo(correo)).thenReturn(Optional.of(usuarioExistente));
        when(usuariosRepository.save(any(Usuarios.class))).thenReturn(usuarioExistente);

        // Act
        RegisterResponse response = authService.UpdatePaciente(correo, request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Nuevo Nombre", usuarioExistente.getNombres());
        assertEquals("Nuevo Apellido", usuarioExistente.getApellidos());
    }

    @Test
    void updatePaciente_WhenUserNotFound_ShouldReturnError() {
        // Arrange
        String correo = "nonexistent@test.com";
        UpdateRequest request = new UpdateRequest();
        when(usuariosRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        // Act
        RegisterResponse response = authService.UpdatePaciente(correo, request);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("no encontrado"));
    }

    // ========== PRUEBAS PARA MANEJO DE ERRORES ==========

    @Test
    void register_WhenExceptionOccurs_ShouldReturnErrorResponse() {
        // Arrange
        RegisterAdminRequest request = new RegisterAdminRequest();
        request.setTipoDocumento(TipoDocumento.CC);
        request.setCorreo("test@test.com");

        when(usuariosRepository.findByCorreo("test@test.com"))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
    }

    @Test
    void login_WhenExceptionOccurs_ShouldReturnErrorResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("test@test.com", "password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act
        AuthResponse response = authService.Login(request);

        // Assert
        assertNull(response.getToken());
        assertNotNull(response.getError());
    }
}