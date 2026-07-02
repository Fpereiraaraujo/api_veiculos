package com.fernando.veiculos.framework.out.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "veiculos")
public class VeiculoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 8)
    private String placa;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 120)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, length = 40)
    private String cor;

    @Column(name = "preco_usd", nullable = false, precision = 14, scale = 2)
    private BigDecimal precoUsd;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public VeiculoEntity() {
    }

    public VeiculoEntity(UUID id, String placa, String marca, String modelo, Integer ano, String cor,
                         BigDecimal precoUsd, boolean ativo, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.precoUsd = precoUsd;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public String getCor() {
        return cor;
    }

    public BigDecimal getPrecoUsd() {
        return precoUsd;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        ativo = true;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public static final class Builder {
        private UUID id;
        private String placa;
        private String marca;
        private String modelo;
        private Integer ano;
        private String cor;
        private BigDecimal precoUsd;
        private boolean ativo;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder placa(String placa) {
            this.placa = placa;
            return this;
        }

        public Builder marca(String marca) {
            this.marca = marca;
            return this;
        }

        public Builder modelo(String modelo) {
            this.modelo = modelo;
            return this;
        }

        public Builder ano(Integer ano) {
            this.ano = ano;
            return this;
        }

        public Builder cor(String cor) {
            this.cor = cor;
            return this;
        }

        public Builder precoUsd(BigDecimal precoUsd) {
            this.precoUsd = precoUsd;
            return this;
        }

        public Builder ativo(boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public VeiculoEntity build() {
            return new VeiculoEntity(id, placa, marca, modelo, ano, cor, precoUsd, ativo, createdAt, updatedAt);
        }
    }
}
