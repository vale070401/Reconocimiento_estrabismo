package com.Deteccion_estrabismo.backend.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "usuarios") // nombre de la coleccion de mongodb
@Data
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
public class Usuarios implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment en PostgreSQL
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombres;
    @Column(nullable = true)
    private Integer documentoIdentidad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private TipoDocumento tipoDocumento;
    @Column(nullable = false, length = 100)
    private String apellidos;
    @Column(nullable = true, length = 100)
    private Date fechaNacimiento;
    @Column(nullable = false, unique = true, length = 100)
    private String correo;
    @Column(nullable = false)
    private String password; // se cifra con Bcrypt
    @Column(name = "numero_tele", length = 20)
    private String numeroTele;
    @Enumerated(EnumType.STRING) // para guardar el rol como texto
    @Column(nullable = false, length = 50)
    private Rol rol; // Pacientes, medicos o administradores
    private boolean enabled;// para activar/desactivar

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getUsername() {
        return this.correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }
}
