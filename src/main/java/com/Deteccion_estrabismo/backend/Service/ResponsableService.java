package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Dto.AuthResponse;
import com.Deteccion_estrabismo.backend.Dto.UpdateResponsableRequest;
import com.Deteccion_estrabismo.backend.Entities.Usuarios;
import com.Deteccion_estrabismo.backend.Entities.Responsable;
import com.Deteccion_estrabismo.backend.Repository.UsuariosRepository;
import com.Deteccion_estrabismo.backend.Repository.ResponsableRepository;
import com.Deteccion_estrabismo.backend.Service.JwtService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponsableService {
    private final ResponsableRepository responsableRepository;
    private final UsuariosRepository usuariosRepository;
    private final JwtService jwtService;
    
    private static final Logger log = LoggerFactory.getLogger(ResponsableService.class);

    public AuthResponse UpdateResponsable(String correo, UpdateResponsableRequest request) {
        try {
            Usuarios usuario = usuariosRepository.findByCorreo(correo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar que sea un Responsable
            if (!(usuario instanceof Responsable)) {
                return AuthResponse.builder()
                        .token(null)
                        .error("El usuario no es un Responsable")
                        .build();
            }

            Responsable responsable = (Responsable) usuario;

            // Actualizar campos de Usuarios (heredados)
            if (request.getNombres() != null && !request.getNombres().isBlank()) {
                responsable.setNombres(request.getNombres());
            }
            if (request.getApellidos() != null && !request.getApellidos().isBlank()) {
                responsable.setApellidos(request.getApellidos());
            }
            if (request.getNumeroTele() != null && !request.getNumeroTele().isBlank()) {
                responsable.setNumeroTele(request.getNumeroTele());
            }

            // Actualizar campos específicos de Responsable
            if (request.getParentesco() != null && !request.getParentesco().isBlank()) {
                responsable.setParentesco(request.getParentesco());
            }
            if (request.getOcupacion() != null && !request.getOcupacion().isBlank()) {
                responsable.setOcupacion(request.getOcupacion());
            }
            if (request.getCiudadResidencia() != null && !request.getCiudadResidencia().isBlank()) {
                responsable.setCiudadResidencia(request.getCiudadResidencia());
            }

            responsableRepository.save(responsable);

            // Generar nuevo token con la información actualizada
            String newToken = jwtService.generateToken(responsable);

            log.info("Responsable actualizado: {} => Nuevo token generado", responsable.getCorreo());

            return AuthResponse.builder()
                    .token(newToken)
                    .error(null)
                    .build();

        } catch (Exception e) {
            log.error("Error actualizando responsable: {}", e.getMessage());
            return AuthResponse.builder()
                    .token(null)
                    .error(e.getMessage())
                    .build();
        }
    }

    
}

