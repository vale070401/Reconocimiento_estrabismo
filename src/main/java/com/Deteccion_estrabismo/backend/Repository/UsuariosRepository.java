package com.Deteccion_estrabismo.backend.Repository;

import com.Deteccion_estrabismo.backend.Entities.Responsable;
import com.Deteccion_estrabismo.backend.Entities.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {

    // Buscar usuario por correo (username en tu caso)
    Optional<Usuarios> findByCorreo(String correo);

    // Verificar si un correo ya está registrado
    boolean existsByCorreo(String correo);

    Optional<Usuarios> findByDocumentoIdentidad(Integer documentoIdentidadResponsable);
}
