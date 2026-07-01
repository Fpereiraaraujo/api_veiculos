package com.fernando.veiculos.application.model;

import java.util.List;

public record PageRequestData(int page, int size, List<SortData> sort) {

    public PageRequestData {
        sort = sort == null ? List.of() : List.copyOf(sort);
    }

    public record SortData(String property, Direction direction) {
    }

    public enum Direction {
        ASC,
        DESC
    }
}
