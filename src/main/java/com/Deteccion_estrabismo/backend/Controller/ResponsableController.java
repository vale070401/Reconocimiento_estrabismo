package com.Deteccion_estrabismo.backend.Controller;

import com.Deteccion_estrabismo.backend.Dto.AuthResponse;
import com.Deteccion_estrabismo.backend.Dto.UpdateResponsableRequest;
import com.Deteccion_estrabismo.backend.Service.ResponsableService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responsables")
public class ResponsableController {

    @Autowired
    private ResponsableService responsableService;

    @PutMapping("/Update")
    public ResponseEntity<AuthResponse> updateResponsable(
            @RequestBody UpdateResponsableRequest request,
            Authentication authentication) {
        
        System.out.println("Recibido UpdateResponsable: " + request);
        
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .token(null)
                            .error("Usuario no autenticado")
                            .build());
        }
        
        String correo = authentication.getName();
        AuthResponse response = responsableService.UpdateResponsable(correo, request);
        
        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}
