package com.Deteccion_estrabismo.backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pacientes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombres;
    @Column(nullable = false)
    private Integer documentoIdentidad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private TipoDocumento tipoDocumento;
    @Column(nullable = false, length = 100)
    private String apellidos;
    @Column(nullable = false, length = 100)
    private Date fechaNacimiento;

    @Column(nullable = false)
    private String genero; // "Masculino", "Femenino", "Otro"

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    @JsonIgnore
    private Responsable responsable; // vínculo con el acudiente

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Evaluacion> evaluaciones = new ArrayList<>();

}
