package com.fernando.veiculos.framework.in.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record VeiculoPatchRequestDTO(
        @Pattern(regexp = "^[A-Za-z]{3}[0-9][A-Za-z0-9][0-9]{2}$", message = "placa deve estar no formato Mercosul ou antigo")
        String placa,

        String marca,

        String modelo,

        @Min(value = 1900, message = "ano deve ser maior ou igual a 1900")
        @Max(value = 2100, message = "ano deve ser menor ou igual a 2100")
        Integer ano,

        String cor,

        @DecimalMin(value = "0.01", message = "precoUsd deve ser maior que zero")
        BigDecimal precoUsd
) {
}
