package com.fernando.veiculos.domain.exception;

import java.util.UUID;


public class VeiculoNotFoundException extends BusinessException {
    public VeiculoNotFoundException(UUID id) {
        super("veiculo nao encontrado com o id: " + id);
    }
}
