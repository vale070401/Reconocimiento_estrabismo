package com.Deteccion_estrabismo.backend.Repository;

import com.Deteccion_estrabismo.backend.Entities.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    List<Evaluacion> findByPacienteId(Long pacienteId);

    List<Evaluacion> findByResultado(boolean resultado);

    List<Evaluacion> findByPacienteResponsableId(Long responsableId);

    List<Evaluacion> findByPacienteDocumentoIdentidad(Integer documentoIdentidad);

}
