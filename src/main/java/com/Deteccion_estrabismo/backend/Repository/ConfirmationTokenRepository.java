package com.Deteccion_estrabismo.backend.Repository;

import com.Deteccion_estrabismo.backend.Entities.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    // busqueda por token por su valor
    Optional<ConfirmationToken> findByToken(String token);

    @Query("SELECT ct FROM ConfirmationToken ct WHERE ct.usuarioId = :usuarioId")
    Optional<ConfirmationToken> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
