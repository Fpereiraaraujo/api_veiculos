package com.fernando.veiculos.domain.service;

import com.fernando.veiculos.domain.exception.BusinessException;
import com.fernando.veiculos.domain.model.Veiculo;

import java.math.BigDecimal;

public final class VeiculoValidator {

    public static final int ANO_MINIMO = 1900;
    public static final int ANO_MAXIMO = 2100;
    private static final BigDecimal PRECO_MINIMO = new BigDecimal("0.01");

    private VeiculoValidator() {
    }

    public static void validarObrigatorio(Veiculo veiculo) {
        validarTextoObrigatorio(veiculo.getPlaca(), "placa e obrigatoria");
        validarTextoObrigatorio(veiculo.getMarca(), "marca e obrigatoria");
        validarTextoObrigatorio(veiculo.getModelo(), "modelo e obrigatorio");
        validarTextoObrigatorio(veiculo.getCor(), "cor e obrigatoria");
        validarAno(veiculo.getAno(), true);
        validarPrecoUsd(veiculo.getPrecoUsd(), true);
    }

    public static void validarPatch(Veiculo veiculo) {
        boolean vazio = veiculo.getPlaca() == null
                && veiculo.getMarca() == null
                && veiculo.getModelo() == null
                && veiculo.getAno() == null
                && veiculo.getCor() == null
                && veiculo.getPrecoUsd() == null;

        if (vazio) {
            throw new BusinessException("informe ao menos um campo para atualizar");
        }

        validarTextoQuandoInformado(veiculo.getMarca(), "marca nao pode ser vazia");
        validarTextoQuandoInformado(veiculo.getModelo(), "modelo nao pode ser vazio");
        validarTextoQuandoInformado(veiculo.getCor(), "cor nao pode ser vazia");
        validarAno(veiculo.getAno(), false);
        validarPrecoUsd(veiculo.getPrecoUsd(), false);
    }

    private static void validarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new BusinessException(mensagem);
        }
    }

    private static void validarTextoQuandoInformado(String valor, String mensagem) {
        if (valor != null && valor.isBlank()) {
            throw new BusinessException(mensagem);
        }
    }

    private static void validarAno(Integer ano, boolean obrigatorio) {
        if (ano == null) {
            if (obrigatorio) {
                throw new BusinessException("ano e obrigatorio");
            }
            return;
        }
        if (ano < ANO_MINIMO || ano > ANO_MAXIMO) {
            throw new BusinessException("ano deve estar entre " + ANO_MINIMO + " e " + ANO_MAXIMO);
        }
    }

    private static void validarPrecoUsd(BigDecimal precoUsd, boolean obrigatorio) {
        if (precoUsd == null) {
            if (obrigatorio) {
                throw new BusinessException("precoUsd e obrigatorio");
            }
            return;
        }
        if (precoUsd.compareTo(PRECO_MINIMO) < 0) {
            throw new BusinessException("precoUsd deve ser maior que zero");
        }
    }
}
