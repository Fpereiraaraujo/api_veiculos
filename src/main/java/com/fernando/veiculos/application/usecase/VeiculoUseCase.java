package com.fernando.veiculos.application.usecase;

import com.fernando.veiculos.application.model.PageRequestData;
import com.fernando.veiculos.application.model.PageResult;
import com.fernando.veiculos.application.port.in.VeiculoPortIn;
import com.fernando.veiculos.application.port.out.CurrencyConversionPortOut;
import com.fernando.veiculos.application.port.out.VeiculoPortOut;
import com.fernando.veiculos.domain.exception.BusinessException;
import com.fernando.veiculos.domain.exception.DuplicatePlacaException;
import com.fernando.veiculos.domain.exception.VeiculoNotFoundException;
import com.fernando.veiculos.domain.model.Veiculo;
import com.fernando.veiculos.domain.service.VeiculoValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Transactional(readOnly = true)
public class VeiculoUseCase implements VeiculoPortIn {

    private final VeiculoPortOut veiculoPortOut;
    private final CurrencyConversionPortOut currencyConversionPortOut;

    public VeiculoUseCase(VeiculoPortOut veiculoPortOut,
                          CurrencyConversionPortOut currencyConversionPortOut) {
        this.veiculoPortOut = veiculoPortOut;
        this.currencyConversionPortOut = currencyConversionPortOut;
    }

    @Override
    public PageResult<Veiculo> buscar(String marca, Integer ano, String cor,
                                      BigDecimal minPreco, BigDecimal maxPreco,
                                      PageRequestData pageRequest) {
        validarRangePreco(minPreco, maxPreco);
        BigDecimal cotacao = currencyConversionPortOut.obterCotacaoUsdParaBrl();
        return veiculoPortOut.findAll(marca, ano, cor, minPreco, maxPreco, pageRequest)
                .map(veiculo -> aplicarPrecoBrl(veiculo, cotacao));
    }

    @Override
    public Veiculo buscarPorId(UUID id) {
        return aplicarPrecoBrl(obterVeiculoAtivo(id));
    }

    @Override
    @Transactional
    public Veiculo cadastrar(Veiculo veiculo) {
        VeiculoValidator.validarObrigatorio(veiculo);
        String placa = normalizarPlaca(veiculo.getPlaca());
        garantirPlacaDisponivel(null, placa);

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
        VeiculoValidator.validarObrigatorio(veiculo);
        Veiculo atual = obterVeiculoAtivo(id);
        String placa = normalizarPlaca(veiculo.getPlaca());
        garantirPlacaDisponivel(id, placa);

        atualizarCamposObrigatorios(atual, veiculo, placa);
        atual.setUpdatedAt(Instant.now());
        return aplicarPrecoBrl(veiculoPortOut.save(atual));
    }

    @Override
    @Transactional
    public Veiculo atualizarParcial(UUID id, Veiculo veiculoParcial) {
        VeiculoValidator.validarPatch(veiculoParcial);
        Veiculo atual = obterVeiculoAtivo(id);

        if (veiculoParcial.getPlaca() != null) {
            String placa = normalizarPlaca(veiculoParcial.getPlaca());
            garantirPlacaDisponivel(id, placa);
            atual.setPlaca(placa);
        }
        atualizarSeInformado(veiculoParcial.getMarca(), atual::setMarca);
        atualizarSeInformado(veiculoParcial.getModelo(), atual::setModelo);
        atualizarSeInformado(veiculoParcial.getAno(), atual::setAno);
        atualizarSeInformado(veiculoParcial.getCor(), atual::setCor);
        atualizarSeInformado(veiculoParcial.getPrecoUsd(), atual::setPrecoUsd);

        atual.setUpdatedAt(Instant.now());
        return aplicarPrecoBrl(veiculoPortOut.save(atual));
    }

    @Override
    @Transactional
    public void remover(UUID id) {
        obterVeiculoAtivo(id);
        veiculoPortOut.softDelete(id);
    }

    @Override
    public List<RelatorioPorMarca> relatorioPorMarca() {
        return veiculoPortOut.countByMarca();
    }

    private void garantirPlacaDisponivel(UUID idAtual, String placa) {
        veiculoPortOut.findByPlaca(placa)
                .filter(v -> idAtual == null || !v.getId().equals(idAtual))
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

    private Veiculo obterVeiculoAtivo(UUID id) {
        return veiculoPortOut.findById(id).orElseThrow(() -> new VeiculoNotFoundException(id));
    }

    private void atualizarCamposObrigatorios(Veiculo atual, Veiculo dados, String placa) {
        atual.setPlaca(placa);
        atual.setMarca(dados.getMarca());
        atual.setModelo(dados.getModelo());
        atual.setAno(dados.getAno());
        atual.setCor(dados.getCor());
        atual.setPrecoUsd(dados.getPrecoUsd());
    }

    private <T> void atualizarSeInformado(T valor, Consumer<T> atualizador) {
        if (valor != null) {
            atualizador.accept(valor);
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
