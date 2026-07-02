package com.fernando.veiculos.framework.in.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VeiculoResponseDTO(
        UUID id,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        BigDecimal precoUsd,
        BigDecimal precoBrl,
        boolean ativo,
        Instant createdAt,
        Instant updatedAt
) {
}
