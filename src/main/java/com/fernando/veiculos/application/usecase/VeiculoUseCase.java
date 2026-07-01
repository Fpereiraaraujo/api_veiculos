package com.fernando.veiculos.application.usecase;

import com.fernando.veiculos.application.port.in.VeiculoPortIn;
import com.fernando.veiculos.application.port.out.CurrencyConversionPortOut;
import com.fernando.veiculos.application.port.out.VeiculoPortOut;
import com.fernando.veiculos.domain.exception.BusinessException;
import com.fernando.veiculos.domain.exception.DuplicatePlacaException;
import com.fernando.veiculos.domain.exception.VeiculoNotFoundException;
import com.fernando.veiculos.domain.model.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class VeiculoUseCase implements VeiculoPortIn {

    private final VeiculoPortOut veiculoPortOut;
    private final CurrencyConversionPortOut currencyConversionPortOut;

    public VeiculoUseCase(VeiculoPortOut veiculoPortOut,
                          CurrencyConversionPortOut currencyConversionPortOut) {
        this.veiculoPortOut = veiculoPortOut;
        this.currencyConversionPortOut = currencyConversionPortOut;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Veiculo> buscar(String marca, Integer ano, String cor,
                                BigDecimal minPreco, BigDecimal maxPreco,
                                Pageable pageable) {
        validarRangePreco(minPreco, maxPreco);
        BigDecimal cotacao = currencyConversionPortOut.obterCotacaoUsdParaBrl();
        return veiculoPortOut.findAll(marca, ano, cor, minPreco, maxPreco, pageable)
                .map(veiculo -> aplicarPrecoBrl(veiculo, cotacao));
    }

    @Override
    @Transactional(readOnly = true)
    public Veiculo buscarPorId(UUID id) {
        return aplicarPrecoBrl(veiculoPortOut.findById(id).orElseThrow(() -> new VeiculoNotFoundException(id)));
    }

    @Override
    @Transactional
    public Veiculo cadastrar(Veiculo veiculo) {
        String placa = normalizarPlaca(veiculo.getPlaca());
        veiculoPortOut.findByPlaca(placa).ifPresent(v -> {
            throw new DuplicatePlacaException(placa);
        });

        Instant now = Instant.now();
        veiculo.setId(null);
        veiculo.setPlaca(placa);
        veiculo.setAtivo(true);
        veiculo.setCreatedAt(now);
        veiculo.setUpdatedAt(now);
        return aplicarPrecoBrl(veiculoPortOut.save(veiculo));
    }

    @Override
    @Transactional
    public Veiculo atualizar(UUID id, Veiculo veiculo) {
        Veiculo atual = buscarPorId(id);
        String placa = normalizarPlaca(veiculo.getPlaca());
        garantirPlacaDisponivel(id, placa);

        atual.setPlaca(placa);
        atual.setMarca(veiculo.getMarca());
        atual.setModelo(veiculo.getModelo());
        atual.setAno(veiculo.getAno());
        atual.setCor(veiculo.getCor());
        atual.setPrecoUsd(veiculo.getPrecoUsd());
        atual.setUpdatedAt(Instant.now());
        return aplicarPrecoBrl(veiculoPortOut.save(atual));
    }

    @Override
    @Transactional
    public Veiculo atualizarParcial(UUID id, Veiculo veiculoParcial) {
        validarPatch(veiculoParcial);
        Veiculo atual = buscarPorId(id);

        if (veiculoParcial.getPlaca() != null) {
            String placa = normalizarPlaca(veiculoParcial.getPlaca());
            garantirPlacaDisponivel(id, placa);
            atual.setPlaca(placa);
        }
        if (veiculoParcial.getMarca() != null) {
            atual.setMarca(veiculoParcial.getMarca());
        }
        if (veiculoParcial.getModelo() != null) {
            atual.setModelo(veiculoParcial.getModelo());
        }
        if (veiculoParcial.getAno() != null) {
            atual.setAno(veiculoParcial.getAno());
        }
        if (veiculoParcial.getCor() != null) {
            atual.setCor(veiculoParcial.getCor());
        }
        if (veiculoParcial.getPrecoUsd() != null) {
            atual.setPrecoUsd(veiculoParcial.getPrecoUsd());
        }

        atual.setUpdatedAt(Instant.now());
        return aplicarPrecoBrl(veiculoPortOut.save(atual));
    }

    @Override
    @Transactional
    public void remover(UUID id) {
        buscarPorId(id);
        veiculoPortOut.softDelete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelatorioPorMarca> relatorioPorMarca() {
        return veiculoPortOut.countByMarca();
    }

    private void garantirPlacaDisponivel(UUID idAtual, String placa) {
        veiculoPortOut.findByPlaca(placa)
                .filter(v -> !v.getId().equals(idAtual))
                .ifPresent(v -> {
                    throw new DuplicatePlacaException(placa);
                });
    }

    private String normalizarPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new BusinessException("placa e obrigatoria");
        }
        return placa.trim().toUpperCase();
    }

    private void validarRangePreco(BigDecimal minPreco, BigDecimal maxPreco) {
        if (minPreco != null && maxPreco != null && minPreco.compareTo(maxPreco) > 0) {
            throw new BusinessException("minPreco nao pode ser maior que maxPreco");
        }
    }

    private void validarPatch(Veiculo veiculoParcial) {
        boolean vazio = veiculoParcial.getPlaca() == null
                && veiculoParcial.getMarca() == null
                && veiculoParcial.getModelo() == null
                && veiculoParcial.getAno() == null
                && veiculoParcial.getCor() == null
                && veiculoParcial.getPrecoUsd() == null;

        if (vazio) {
            throw new BusinessException("informe ao menos um campo para atualizar");
        }
    }

    private Veiculo aplicarPrecoBrl(Veiculo veiculo) {
        return aplicarPrecoBrl(veiculo, currencyConversionPortOut.obterCotacaoUsdParaBrl());
    }

    private Veiculo aplicarPrecoBrl(Veiculo veiculo, BigDecimal cotacao) {
        if (veiculo.getPrecoUsd() != null && cotacao != null) {
            veiculo.setPrecoBrl(veiculo.getPrecoUsd().multiply(cotacao).setScale(2, RoundingMode.HALF_UP));
        }
        return veiculo;
    }
}
