package com.Deteccion_estrabismo.backend.Dto;

import com.Deteccion_estrabismo.backend.Entities.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private boolean success;
    private String error;


    public static RegisterResponse success() {
        return RegisterResponse.builder()
                .success(true)
                .error(null)
                .build();
    }

    public static RegisterResponse error(String message) {
        return RegisterResponse.builder()
                .success(false)
                .error(message)
                .build();
    }
}