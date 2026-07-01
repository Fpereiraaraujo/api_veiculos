package com.fernando.veiculos.application.port.in;

import com.fernando.veiculos.domain.model.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface VeiculoPortIn {

    Page<Veiculo> buscar(String marca, Integer ano, String cor,
                          BigDecimal minPreco, BigDecimal maxPreco,
                          Pageable pageable);

    Veiculo buscarPorId(UUID id);

    Veiculo cadastrar(Veiculo veiculo);

    Veiculo atualizar(UUID id, Veiculo veiculo);

    Veiculo atualizarParcial(UUID id, Veiculo veiculoParcial);

    void remover(UUID id);
}
