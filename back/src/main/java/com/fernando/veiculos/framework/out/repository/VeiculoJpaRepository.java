package com.fernando.veiculos.framework.out.repository;

import com.fernando.veiculos.framework.out.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, UUID>,
        JpaSpecificationExecutor<VeiculoEntity> {

    Optional<VeiculoEntity> findByIdAndAtivoTrue(UUID id);

    Optional<VeiculoEntity> findByPlaca(String placa);

    @Query("""
            select v.marca, count(v.id)
            from VeiculoEntity v
            where v.ativo = true
            group by v.marca
            order by v.marca
            """)
    List<Object[]> countActiveByMarca();
}
