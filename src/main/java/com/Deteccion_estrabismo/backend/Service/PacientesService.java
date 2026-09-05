package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Entities.Pacientes;
import com.Deteccion_estrabismo.backend.Repository.PacientesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacientesService {
    @Autowired
    private PacientesRepository pacientesRepository;

    public List<Pacientes> findAll() {
        return pacientesRepository.findAll();
    }

    public Pacientes findById(Long id) {
        return pacientesRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        pacientesRepository.deleteById(id);
    }

    public List<Pacientes> findbyResponsableId(Long responsableId) {
        return pacientesRepository.findByResponsableId(responsableId);
    }
}
