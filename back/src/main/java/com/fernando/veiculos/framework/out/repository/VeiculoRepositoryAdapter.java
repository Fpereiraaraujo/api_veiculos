package com.fernando.veiculos.framework.out.repository;

import com.fernando.veiculos.application.model.PageRequestData;
import com.fernando.veiculos.application.model.PageResult;
import com.fernando.veiculos.application.port.in.VeiculoPortIn.RelatorioPorMarca;
import com.fernando.veiculos.application.port.out.VeiculoPortOut;
import com.fernando.veiculos.domain.model.Veiculo;
import com.fernando.veiculos.framework.out.entity.VeiculoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VeiculoRepositoryAdapter implements VeiculoPortOut {

    private final VeiculoJpaRepository repository;

    public VeiculoRepositoryAdapter(VeiculoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Veiculo> findAll(String marca, Integer ano, String cor,
                                       BigDecimal minPreco, BigDecimal maxPreco,
                                       PageRequestData pageRequest) {
        Page<VeiculoEntity> page = repository.findAll(
                VeiculoSpecifications.filtros(marca, ano, cor, minPreco, maxPreco),
                toPageable(pageRequest)
        );
        return toPageResult(page);
    }

    @Override
    public Optional<Veiculo> findById(UUID id) {
        return repository.findByIdAndAtivoTrue(id).map(this::toDomain);
    }

    @Override
    public Optional<Veiculo> findByPlaca(String placa) {
        return repository.findByPlaca(placa).map(this::toDomain);
    }

    @Override
    public Veiculo save(Veiculo veiculo) {
        return toDomain(repository.save(toEntity(veiculo)));
    }

    @Override
    public void softDelete(UUID id) {
        repository.findByIdAndAtivoTrue(id).ifPresent(entity -> {
            entity.setAtivo(false);
            repository.save(entity);
        });
    }

    @Override
    public List<RelatorioPorMarca> countByMarca() {
        return repository.countActiveByMarca().stream()
                .map(row -> new RelatorioPorMarca((String) row[0], (Long) row[1]))
                .toList();
    }

    private Veiculo toDomain(VeiculoEntity entity) {
        return Veiculo.builder()
                .id(entity.getId())
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .ano(entity.getAno())
                .cor(entity.getCor())
                .precoUsd(entity.getPrecoUsd())
                .ativo(entity.isAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Pageable toPageable(PageRequestData pageRequest) {
        List<Sort.Order> orders = pageRequest.sort().stream()
                .map(sort -> new Sort.Order(toSpringDirection(sort.direction()), sort.property()))
                .toList();
        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        return PageRequest.of(pageRequest.page(), pageRequest.size(), sort);
    }

    private Sort.Direction toSpringDirection(PageRequestData.Direction direction) {
        return direction == PageRequestData.Direction.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private PageResult<Veiculo> toPageResult(Page<VeiculoEntity> page) {
        return new PageResult<>(
                page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private VeiculoEntity toEntity(Veiculo veiculo) {
        return VeiculoEntity.builder()
                .id(veiculo.getId())
                .placa(veiculo.getPlaca())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .ano(veiculo.getAno())
                .cor(veiculo.getCor())
                .precoUsd(veiculo.getPrecoUsd())
                .ativo(veiculo.isAtivo())
                .createdAt(veiculo.getCreatedAt())
                .updatedAt(veiculo.getUpdatedAt())
                .build();
    }
}
