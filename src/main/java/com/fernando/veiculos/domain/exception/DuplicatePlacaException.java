package com.fernando.veiculos.domain.exception;


public class DuplicatePlacaException extends BusinessException {
    public DuplicatePlacaException(String placa) {
        super("ja existe um veiculo cadastrado com a placa: " + placa);
    }
}
