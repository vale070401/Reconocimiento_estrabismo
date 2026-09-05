package com.Deteccion_estrabismo.backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@EqualsAndHashCode(callSuper = true)

@Entity
@Table(name="administradores")
@PrimaryKeyJoinColumn(name="usuario_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrador  extends Usuarios{
    @Column(length = 100)
    private String cargo; //"Soporte tecnico", "Coordinador General" etc

    @Column(length = 255)
    private String permisos; //descripcion general de privilegios

    @Column(length = 100)
    private String areResponsable;//"Gestion de usuarios", "Validacion Medica" etc

}
