package com.Deteccion_estrabismo.backend.Repository;

import com.Deteccion_estrabismo.backend.Entities.Pacientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacientesRepository extends JpaRepository<Pacientes, Long> {
    Optional<Pacientes> findById(Long id);
    Optional<Pacientes> findByDocumentoIdentidad(Integer documentoIdentidad);
    List<Pacientes> findByResponsableId(Long responsableId);
}
