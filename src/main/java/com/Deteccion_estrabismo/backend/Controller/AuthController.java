package com.Deteccion_estrabismo.backend.Controller;

import com.Deteccion_estrabismo.backend.Dto.*;
import com.Deteccion_estrabismo.backend.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Se valida el usuario y se genera el token
        return ResponseEntity.ok(authService.Login(request));

    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/confirm")
    public AuthResponse confirm(@RequestParam String token) {
        return authService.confirmToken(token);
    }

    @PostMapping("/register/paciente")
    public ResponseEntity<RegisterResponse> registerPaciente(@RequestBody RegisterPacienteRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/responsable")
    public ResponseEntity<RegisterResponse> registerResponsable(@RequestBody RegisterResponsableRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<RegisterResponse> registerAdmin(@RequestBody RegisterAdminRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

}
