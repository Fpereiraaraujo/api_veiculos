package com.fernando.veiculos.framework.out.repository;

import com.fernando.veiculos.framework.out.entity.VeiculoEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class VeiculoSpecifications {

    private VeiculoSpecifications() {
    }

    public static Specification<VeiculoEntity> filtros(String marca, Integer ano, String cor,
                                                       BigDecimal minPreco, BigDecimal maxPreco) {
        return ativo()
                .and(contendoMarca(marca))
                .and(comAno(ano))
                .and(contendoCor(cor))
                .and(precoMaiorOuIgual(minPreco))
                .and(precoMenorOuIgual(maxPreco));
    }

    private static Specification<VeiculoEntity> ativo() {
        return (root, query, cb) -> cb.isTrue(root.get("ativo"));
    }

    private static Specification<VeiculoEntity> contendoMarca(String marca) {
        return (root, query, cb) -> marca == null || marca.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("marca")), "%" + marca.toLowerCase() + "%");
    }

    private static Specification<VeiculoEntity> comAno(Integer ano) {
        return (root, query, cb) -> ano == null ? cb.conjunction() : cb.equal(root.get("ano"), ano);
    }

    private static Specification<VeiculoEntity> contendoCor(String cor) {
        return (root, query, cb) -> cor == null || cor.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("cor")), "%" + cor.toLowerCase() + "%");
    }

    private static Specification<VeiculoEntity> precoMaiorOuIgual(BigDecimal minPreco) {
        return (root, query, cb) -> minPreco == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("precoUsd"), minPreco);
    }

    private static Specification<VeiculoEntity> precoMenorOuIgual(BigDecimal maxPreco) {
        return (root, query, cb) -> maxPreco == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("precoUsd"), maxPreco);
    }
}
