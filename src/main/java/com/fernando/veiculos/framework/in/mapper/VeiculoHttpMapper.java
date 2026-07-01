package com.fernando.veiculos.framework.in.mapper;

import com.fernando.veiculos.application.port.in.VeiculoPortIn.RelatorioPorMarca;
import com.fernando.veiculos.domain.model.Veiculo;
import com.fernando.veiculos.framework.in.dto.RelatorioPorMarcaDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoPatchRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class VeiculoHttpMapper {

    public Veiculo toDomain(VeiculoRequestDTO request) {
        return Veiculo.builder()
                .placa(request.placa())
                .marca(request.marca())
                .modelo(request.modelo())
                .ano(request.ano())
                .cor(request.cor())
                .precoUsd(request.precoUsd())
                .build();
    }

    public Veiculo toDomain(VeiculoPatchRequestDTO request) {
        return Veiculo.builder()
                .placa(request.placa())
                .marca(request.marca())
                .modelo(request.modelo())
                .ano(request.ano())
                .cor(request.cor())
                .precoUsd(request.precoUsd())
                .build();
    }

    public VeiculoResponseDTO toResponse(Veiculo veiculo) {
        return new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPrecoUsd(),
                veiculo.getPrecoBrl(),
                veiculo.isAtivo(),
                veiculo.getCreatedAt(),
                veiculo.getUpdatedAt()
        );
    }

    public RelatorioPorMarcaDTO toResponse(RelatorioPorMarca item) {
        return new RelatorioPorMarcaDTO(item.marca(), item.quantidade());
    }
}
