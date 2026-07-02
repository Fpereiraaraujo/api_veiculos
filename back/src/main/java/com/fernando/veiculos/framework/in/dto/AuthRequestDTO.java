package com.fernando.veiculos.framework.in.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank(message = "username e obrigatorio")
        String username,

        @NotBlank(message = "password e obrigatorio")
        String password
) {
}
