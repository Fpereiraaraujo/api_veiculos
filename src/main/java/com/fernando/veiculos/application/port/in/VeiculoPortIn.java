package com.fernando.veiculos.application.port.in;

import com.fernando.veiculos.application.model.PageRequestData;
import com.fernando.veiculos.application.model.PageResult;
import com.fernando.veiculos.domain.model.Veiculo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface VeiculoPortIn {

    PageResult<Veiculo> buscar(String marca, Integer ano, String cor,
                                BigDecimal minPreco, BigDecimal maxPreco,
                                PageRequestData pageRequest);

    Veiculo buscarPorId(UUID id);

    Veiculo cadastrar(Veiculo veiculo);

    Veiculo atualizar(UUID id, Veiculo veiculo);

    Veiculo atualizarParcial(UUID id, Veiculo veiculoParcial);

    void remover(UUID id);

    List<RelatorioPorMarca> relatorioPorMarca();

    record RelatorioPorMarca(String marca, long quantidade) {
    }
}
