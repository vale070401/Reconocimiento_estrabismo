package com.Deteccion_estrabismo.backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)

@Entity
@Table(name = "responsables")
@PrimaryKeyJoinColumn(name = "usuario_id")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Responsable extends Usuarios {
    @Column(nullable = true, length = 50)
    private String parentesco; // "Padre", "Madre", "Tío", "Tutor", etc.

    @Column(length = 50)
    private String ocupacion; // trabajo o profesión

    @OneToMany(mappedBy = "responsable", cascade = CascadeType.ALL)
    private List<Pacientes> pacientes; // hijos o niños a cargo

    @Column(length = 100)
    private String ciudadResidencia;
}
