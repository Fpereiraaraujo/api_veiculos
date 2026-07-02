package com.fernando.veiculos.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Veiculo {
    private UUID id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private BigDecimal precoUsd;
    private BigDecimal precoBrl;
    private boolean ativo;
    private Instant createdAt;
    private Instant updatedAt;

    public Veiculo() {
    }

    public Veiculo(UUID id, String placa, String marca, String modelo, Integer ano, String cor,
                   BigDecimal precoUsd, BigDecimal precoBrl, boolean ativo, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.precoUsd = precoUsd;
        this.precoBrl = precoBrl;
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

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public BigDecimal getPrecoUsd() {
        return precoUsd;
    }

    public void setPrecoUsd(BigDecimal precoUsd) {
        this.precoUsd = precoUsd;
    }

    public BigDecimal getPrecoBrl() {
        return precoBrl;
    }

    public void setPrecoBrl(BigDecimal precoBrl) {
        this.precoBrl = precoBrl;
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

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static final class Builder {
        private UUID id;
        private String placa;
        private String marca;
        private String modelo;
        private Integer ano;
        private String cor;
        private BigDecimal precoUsd;
        private BigDecimal precoBrl;
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

        public Builder precoBrl(BigDecimal precoBrl) {
            this.precoBrl = precoBrl;
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

        public Veiculo build() {
            return new Veiculo(id, placa, marca, modelo, ano, cor, precoUsd, precoBrl, ativo, createdAt, updatedAt);
        }
    }
}
