package com.fernando.veiculos.framework.in.mapper;

import com.fernando.veiculos.application.model.PageRequestData;
import com.fernando.veiculos.application.model.PageResult;
import com.fernando.veiculos.application.port.in.VeiculoPortIn.RelatorioPorMarca;
import com.fernando.veiculos.domain.model.Veiculo;
import com.fernando.veiculos.framework.in.dto.RelatorioPorMarcaDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoPatchRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoResponseDTO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public PageRequestData toPageRequest(Pageable pageable) {
        List<PageRequestData.SortData> sort = pageable.getSort().stream()
                .map(this::toSortData)
                .toList();
        return new PageRequestData(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    public PagedModel<VeiculoResponseDTO> toPagedResponse(PageResult<Veiculo> page, Pageable pageable) {
        List<VeiculoResponseDTO> content = page.content().stream()
                .map(this::toResponse)
                .toList();
        return new PagedModel<>(new PageImpl<>(content, pageable, page.totalElements()));
    }

    private PageRequestData.SortData toSortData(Sort.Order order) {
        PageRequestData.Direction direction = order.isDescending()
                ? PageRequestData.Direction.DESC
                : PageRequestData.Direction.ASC;
        return new PageRequestData.SortData(order.getProperty(), direction);
    }
}
