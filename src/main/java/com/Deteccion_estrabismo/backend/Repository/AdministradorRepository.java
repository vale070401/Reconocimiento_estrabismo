package com.Deteccion_estrabismo.backend.Repository;

import com.Deteccion_estrabismo.backend.Entities.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

}
