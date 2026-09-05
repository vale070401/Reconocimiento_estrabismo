package com.Deteccion_estrabismo.backend.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.Id;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "confirmacion_Token")
@Entity
public class ConfirmationToken{
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String usuarioId;

    public ConfirmationToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt, String usuarioId) {
        this.token = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusMinutes(15);
        this.usuarioId = usuarioId;
    }



}
