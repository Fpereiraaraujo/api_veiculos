package com.fernando.veiculos.framework.in.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record VeiculoRequestDTO(
        @NotBlank(message = "placa e obrigatoria")
        @Pattern(regexp = "^[A-Za-z]{3}[0-9][A-Za-z0-9][0-9]{2}$", message = "placa deve estar no formato Mercosul ou antigo")
        String placa,

        @NotBlank(message = "marca e obrigatoria")
        String marca,

        @NotBlank(message = "modelo e obrigatorio")
        String modelo,

        @NotNull(message = "ano e obrigatorio")
        @Min(value = 1900, message = "ano deve ser maior ou igual a 1900")
        @Max(value = 2100, message = "ano deve ser menor ou igual a 2100")
        Integer ano,

        @NotBlank(message = "cor e obrigatoria")
        String cor,

        @NotNull(message = "precoUsd e obrigatorio")
        @DecimalMin(value = "0.01", message = "precoUsd deve ser maior que zero")
        BigDecimal precoUsd
) {
}
