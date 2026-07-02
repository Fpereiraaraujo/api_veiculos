package com.fernando.veiculos.application.port.out;

import com.fernando.veiculos.application.model.PageRequestData;
import com.fernando.veiculos.application.model.PageResult;
import com.fernando.veiculos.application.port.in.VeiculoPortIn.RelatorioPorMarca;
import com.fernando.veiculos.domain.model.Veiculo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface VeiculoPortOut {

    PageResult<Veiculo> findAll(String marca, Integer ano, String cor,
                                 BigDecimal minPreco, BigDecimal maxPreco,
                                 PageRequestData pageRequest);

    Optional<Veiculo> findById(UUID id);

    Optional<Veiculo> findByPlaca(String placa);

    Veiculo save(Veiculo veiculo);


    void softDelete(UUID id);

    List<RelatorioPorMarca> countByMarca();
}
