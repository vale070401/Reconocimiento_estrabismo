package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Dto.*;
import com.Deteccion_estrabismo.backend.Entities.*;
import com.Deteccion_estrabismo.backend.Repository.UsuariosRepository;
import com.Deteccion_estrabismo.backend.Repository.PacientesRepository;
import com.Deteccion_estrabismo.backend.Repository.ResponsableRepository;
import com.Deteccion_estrabismo.backend.Repository.AdministradorRepository;
import com.Deteccion_estrabismo.backend.util.BuildObjectMapper;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {
    private UsuariosRepository usuariosRepository;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private JwtService jwtService;
    private final BuildObjectMapper mapper;
    private final PacientesRepository pacientesRepository;
    private final AdministradorRepository administradorRepository;

    private final ResponsableRepository responsableRepository;
    private AuthenticationManager authenticationManager;

    public AuthService(AdministradorRepository administradorRepository, BuildObjectMapper mapper,
            PacientesRepository pacientesRepository,
            ResponsableRepository responsableRepository, AuthenticationManager authenticationManager,
            JwtService jwtService, UsuariosRepository usuariosRepository) {
        this.administradorRepository = administradorRepository;
        this.mapper = mapper;
        this.pacientesRepository = pacientesRepository;

        this.responsableRepository = responsableRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuariosRepository = usuariosRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse Login(LoginRequest request) {
        try {
            // validar usuarios y password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getCorreo(),
                            request.getPassword()));

            // buscar usuario
            Usuarios usuarios = usuariosRepository.findByCorreo(request.getCorreo())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // generacion de token
            String token = jwtService.generateToken(usuarios);

            // Log de depuración
            log.info("Usuario logueado: {} con rol: {} => Token generado: {}",
                    usuarios.getCorreo(), usuarios.getRol(), token);

            return AuthResponse.builder()
                    .token(token)
                    .error(null)
                    .build();

        } catch (Exception e) {
            return AuthResponse.builder()
                    .token(null)
                    .error(e.getMessage())
                    .build();
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        try {
            // Validar correo único
            if (usuariosRepository.findByCorreo(request.getCorreo()).isPresent()) {
                return RegisterResponse.error("El correo ya está registrado");
            }

            if (request.getTipoDocumento() == null) {
                return RegisterResponse.error("El tipo de documento es obligatorio");
            }

            switch (request.getTipoDocumento()) {
                case REGISTRO_CIVIL, TI, NUIP -> {
                    if (request instanceof RegisterPacienteRequest pacienteRequest) {
                        crearPaciente(pacienteRequest);
                        return RegisterResponse.success();
                    } else {
                        return RegisterResponse.error("La estructura del request no corresponde a un Paciente");
                    }
                }
                case CC, CE, PASAPORTE -> {
                    if (request instanceof RegisterResponsableRequest responsableRequest) {
                        crearResponsable(responsableRequest);
                    } else if (request instanceof RegisterAdminRequest adminRequest) {
                        crearAdministrador(adminRequest);
                    } else {
                        return RegisterResponse
                                .error("La estructura del request no corresponde a un Responsable/Admin");
                    }
                }
                default -> {
                    return RegisterResponse.error("Tipo de documento no soportado");
                }
            }

            return RegisterResponse.success();

        } catch (Exception e) {
            log.error("Error en registro: {}", e.getMessage());
            return RegisterResponse.error(e.getMessage());
        }
    }

    private Pacientes crearPaciente(RegisterPacienteRequest request) {
        Pacientes paciente = mapper.converterTo(request, Pacientes.class);

        if (request.getDocumentoIdentidadResponsable() != null) {
            Usuarios responsable = usuariosRepository
                    .findByDocumentoIdentidad(request.getDocumentoIdentidadResponsable())
                    .orElseThrow(() -> new RuntimeException("Responsable no encontrado"));
            paciente.setResponsable((Responsable) responsable);
        }

        return pacientesRepository.save(paciente);
    }

    private Responsable crearResponsable(RegisterResponsableRequest request) {
        Responsable responsable = mapper.converterTo(request, Responsable.class);
        responsable.setPassword(passwordEncoder.encode(request.getPassword()));
        responsable.setRol(request.getRol());
        responsable.setEnabled(true);
        return responsableRepository.save(responsable);
    }

    private Administrador crearAdministrador(RegisterAdminRequest request) {
        Administrador administrador = mapper.converterTo(request, Administrador.class);
        administrador.setPassword(passwordEncoder.encode(request.getPassword()));
        administrador.setRol(request.getRol());
        administrador.setEnabled(true);
        return administradorRepository.save(administrador);
    }

    public RegisterResponse UpdatePaciente(String correo, UpdateRequest request) {
        try {
            Usuarios usuario = (usuariosRepository.findByCorreo(correo)
                    .orElseThrow(() -> new RuntimeException(("usuario no encontrado"))));
            // Actualizar campos si no son nulos
            // Actualizar campos si son válidos
            if (request.getNombres() != null && !request.getNombres().isBlank()) {
                usuario.setNombres(request.getNombres());
            }
            if (request.getApellidos() != null && !request.getApellidos().isBlank()) {
                usuario.setApellidos(request.getApellidos());
            }
            if (request.getNumeroTele() != null && !request.getNumeroTele().isBlank()) {
                usuario.setNumeroTele(request.getNumeroTele());
            }

            usuariosRepository.save(usuario);
            return RegisterResponse.builder()
                    .success(true)
                    .error(null)
                    .build();

        } catch (Exception e) {
            return RegisterResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

}
