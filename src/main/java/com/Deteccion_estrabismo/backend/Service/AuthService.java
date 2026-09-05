package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Dto.*;
import com.Deteccion_estrabismo.backend.Entities.*;
import com.Deteccion_estrabismo.backend.Repository.ConfirmationTokenRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;

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
    private ConfirmationTokenRepository tokenRepository;
    private AuthenticationManager authenticationManager;
    private SendGridEmailService emailService;

    public AuthService(AdministradorRepository administradorRepository, BuildObjectMapper mapper,
            PacientesRepository pacientesRepository,
            ResponsableRepository responsableRepository, AuthenticationManager authenticationManager,
            SendGridEmailService emailService, JwtService jwtService,
            ConfirmationTokenRepository tokenRepository, UsuariosRepository usuariosRepository) {
        this.administradorRepository = administradorRepository;
        this.mapper = mapper;
        this.pacientesRepository = pacientesRepository;

        this.responsableRepository = responsableRepository;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
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

    public AuthResponse confirmToken(String token) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token inválido"));

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expirado");
        }

        Usuarios usuario = usuariosRepository.findById(Long.valueOf(confirmationToken.getUsuarioId()))
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        usuario.setEnabled(true);
        usuariosRepository.save(usuario);

        // 🚨 Ahora sí generamos JWT
        String jwt = jwtService.generateToken(usuario);

        return new AuthResponse(jwt, null);
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

            Usuarios usuario = null;

            switch (request.getTipoDocumento()) {
                case REGISTRO_CIVIL, TI, NUIP -> {
                    if (request instanceof RegisterPacienteRequest pacienteRequest) {
                        crearPaciente(pacienteRequest);
                        return RegisterResponse.success(); // Paciente no requiere confirmación por correo
                    } else {
                        return RegisterResponse.error("La estructura del request no corresponde a un Paciente");
                    }
                }
                case CC, CE, PASAPORTE -> {
                    if (request instanceof RegisterResponsableRequest responsableRequest) {
                        usuario = crearResponsable(responsableRequest);
                    } else if (request instanceof RegisterAdminRequest adminRequest) {
                        usuario = crearAdministrador(adminRequest);
                    } else {
                        return RegisterResponse
                                .error("La estructura del request no corresponde a un Responsable/Admin");
                    }
                }
                default -> {
                    return RegisterResponse.error("Tipo de documento no soportado");
                }
            }

            if (usuario != null) {
                // Generar y enviar token solo para Responsables/Admin (son Usuarios)
                enviarTokenConfirmacion(usuario);
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
        responsable.setEnabled(false);
        return responsableRepository.save(responsable);
    }

    private Administrador crearAdministrador(RegisterAdminRequest request) {
        Administrador administrador = mapper.converterTo(request, Administrador.class);
        administrador.setPassword(passwordEncoder.encode(request.getPassword()));
        administrador.setRol(request.getRol());
        administrador.setEnabled(false);
        return administradorRepository.save(administrador);
    }

    private void enviarTokenConfirmacion(Usuarios usuario) {
        String confirmationToken = UUID.randomUUID().toString();

        ConfirmationToken tokenEntity = ConfirmationToken.builder()
                .token(confirmationToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .usuarioId(String.valueOf(usuario.getId()))
                .build();

        tokenRepository.save(tokenEntity);

        String link = "https://reconocimiento-estrabismo.onrender.com/auth/confirm?token=" + confirmationToken;

        // Usar el servicio de email
        emailService.sendEmail(
                usuario.getCorreo(),
                "Confirma tu cuenta en Detecteye - " + usuario.getRol(),
                construirMensajeEmail(usuario, link));
    }

    private String construirMensajeEmail(Usuarios usuario, String link) {
        return switch (usuario.getRol()) {

            case RESPONSABLE -> "Bienvenido/a " + usuario.getNombres() +
                    ",\n\nConfirma tu cuenta para gestionar pacientes:\n" + link;
            case ADMIN -> "Bienvenido Administrador " + usuario.getNombres() +
                    ",\n\nConfirma tu cuenta para acceder al sistema:\n" + link;
            default -> "Bienvenido " + usuario.getNombres() +
                    ",\n\nConfirma tu cuenta:\n" + link;
        } + "\n\nEl enlace expirará en 24 horas";
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
