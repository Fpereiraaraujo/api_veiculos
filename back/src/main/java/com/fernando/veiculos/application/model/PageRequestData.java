package com.fernando.veiculos.application.model;

import com.fernando.veiculos.domain.exception.BusinessException;

import java.util.List;
import java.util.Set;

public record PageRequestData(int page, int size, List<SortData> sort) {

    public static final int MAX_SIZE = 100;
    public static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "placa",
            "marca",
            "modelo",
            "ano",
            "cor",
            "precoUsd",
            "createdAt",
            "updatedAt"
    );

    public PageRequestData {
        if (page < 0) {
            throw new BusinessException("page deve ser maior ou igual a zero");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new BusinessException("size deve estar entre 1 e " + MAX_SIZE);
        }
        sort = sort == null ? List.of() : List.copyOf(sort);
        sort.forEach(PageRequestData::validarSort);
    }

    private static void validarSort(SortData sort) {
        if (sort == null || sort.property() == null || sort.property().isBlank()) {
            throw new BusinessException("sort deve informar um campo valido");
        }
        if (!ALLOWED_SORT_PROPERTIES.contains(sort.property())) {
            throw new BusinessException("sort nao permitido para o campo: " + sort.property());
        }
    }

    public record SortData(String property, Direction direction) {
    }

    public enum Direction {
        ASC,
        DESC
    }
}
