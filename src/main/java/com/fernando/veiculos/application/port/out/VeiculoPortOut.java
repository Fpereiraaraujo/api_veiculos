package com.fernando.veiculos.application.port.out;

import com.fernando.veiculos.domain.model.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;


public interface VeiculoPortOut {

    Page<Veiculo> findAll(String marca, Integer ano, String cor,
                           BigDecimal minPreco, BigDecimal maxPreco,
                           Pageable pageable);

    Optional<Veiculo> findById(UUID id);

    Optional<Veiculo> findByPlaca(String placa);

    Veiculo save(Veiculo veiculo);


    void softDelete(UUID id);
}
