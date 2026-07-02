package com.fernando.veiculos.framework.in.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDTO> fieldErrors
) {
    public record FieldErrorDTO(String field, String message) {
    }
}
