package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Repository.UsuariosRepository;
import com.Deteccion_estrabismo.backend.Entities.Usuarios;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuariosService {

    private UsuariosRepository usuariosRepository;

    public UsuariosService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Usuarios registrarUsuarios(Usuarios usuarios) {
        usuarios.setPassword(passwordEncoder.encode(usuarios.getPassword())); // Cifra la contraseña que se ingrese
        return usuariosRepository.save(usuarios);

    }
}
