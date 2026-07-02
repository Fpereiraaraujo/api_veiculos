package com.fernando.veiculos.application.usecase;

import com.fernando.veiculos.application.model.PageRequestData;
import com.fernando.veiculos.application.model.PageResult;
import com.fernando.veiculos.application.port.out.CurrencyConversionPortOut;
import com.fernando.veiculos.application.port.out.VeiculoPortOut;
import com.fernando.veiculos.domain.exception.BusinessException;
import com.fernando.veiculos.domain.exception.DuplicatePlacaException;
import com.fernando.veiculos.domain.model.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoUseCaseTest {

    private static final BigDecimal COTACAO = new BigDecimal("5.00");

    @Mock
    private VeiculoPortOut veiculoPortOut;

    @Mock
    private CurrencyConversionPortOut currencyConversionPortOut;

    private VeiculoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new VeiculoUseCase(veiculoPortOut, currencyConversionPortOut);
    }

    @Test
    void deveBloquearCadastroComPlacaDuplicada() {
        when(veiculoPortOut.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo("ABC1D23")));

        assertThatThrownBy(() -> useCase.cadastrar(veiculo("abc1d23")))
                .isInstanceOf(DuplicatePlacaException.class)
                .hasMessageContaining("ABC1D23");

        verify(veiculoPortOut, never()).save(any());
        verifyNoInteractions(currencyConversionPortOut);
    }

    @Test
    void deveValidarRangeDePrecoAntesDeConsultarRepositorio() {
        assertThatThrownBy(() -> useCase.buscar(null, null, null,
                new BigDecimal("20000.00"), new BigDecimal("10000.00"), pageRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("minPreco nao pode ser maior que maxPreco");

        verifyNoInteractions(veiculoPortOut, currencyConversionPortOut);
    }

    @Test
    void deveValidarPaginacaoAntesDeConsultarRepositorio() {
        PageRequestData.SortData sortInvalido = new PageRequestData.SortData("campoInexistente",
                PageRequestData.Direction.ASC);

        assertThatThrownBy(() -> new PageRequestData(0, 20, List.of(sortInvalido)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("sort nao permitido para o campo: campoInexistente");

        verifyNoInteractions(veiculoPortOut, currencyConversionPortOut);
    }

    @Test
    void deveValidarDadosObrigatoriosNoDominioAntesDeCadastrar() {
        Veiculo invalido = veiculo("ABC1D23");
        invalido.setAno(1800);

        assertThatThrownBy(() -> useCase.cadastrar(invalido))
                .isInstanceOf(BusinessException.class)
                .hasMessage("ano deve estar entre 1900 e 2100");

        verifyNoInteractions(veiculoPortOut, currencyConversionPortOut);
    }

    @Test
    void deveValidarPatchVazioAntesDeBuscarVeiculo() {
        assertThatThrownBy(() -> useCase.atualizarParcial(UUID.randomUUID(), Veiculo.builder().build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("informe ao menos um campo para atualizar");

        verifyNoInteractions(veiculoPortOut, currencyConversionPortOut);
    }

    @Test
    void deveAplicarCotacaoUmaVezNaListagem() {
        PageRequestData pageRequest = pageRequest();
        when(currencyConversionPortOut.obterCotacaoUsdParaBrl()).thenReturn(COTACAO);
        when(veiculoPortOut.findAll(eq("Honda"), eq(2021), eq("Preto"),
                eq(null), eq(null), eq(pageRequest)))
                .thenReturn(new PageResult<>(List.of(veiculo("ABC1D23"), veiculo("DEF2E34")), 0, 20, 2, 1));

        var resultado = useCase.buscar("Honda", 2021, "Preto", null, null, pageRequest);

        assertThat(resultado.content())
                .extracting(Veiculo::getPrecoBrl)
                .containsExactly(new BigDecimal("50000.00"), new BigDecimal("50000.00"));
        verify(currencyConversionPortOut).obterCotacaoUsdParaBrl();
    }

    @Test
    void deveAtualizarParcialmenteSomenteCamposInformados() {
        UUID id = UUID.randomUUID();
        Veiculo atual = veiculo("ABC1D23");
        Veiculo patch = Veiculo.builder()
                .cor("Prata")
                .precoUsd(new BigDecimal("12000.00"))
                .build();

        when(veiculoPortOut.findById(id)).thenReturn(Optional.of(atual));
        when(veiculoPortOut.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(currencyConversionPortOut.obterCotacaoUsdParaBrl()).thenReturn(COTACAO);

        Veiculo atualizado = useCase.atualizarParcial(id, patch);

        assertThat(atualizado.getPlaca()).isEqualTo("ABC1D23");
        assertThat(atualizado.getCor()).isEqualTo("Prata");
        assertThat(atualizado.getPrecoUsd()).isEqualByComparingTo("12000.00");
        assertThat(atualizado.getPrecoBrl()).isEqualByComparingTo("60000.00");

        ArgumentCaptor<Veiculo> captor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoPortOut).save(captor.capture());
        assertThat(captor.getValue().getModelo()).isEqualTo("Civic");
    }

    private Veiculo veiculo(String placa) {
        return Veiculo.builder()
                .id(UUID.randomUUID())
                .placa(placa)
                .marca("Honda")
                .modelo("Civic")
                .ano(2021)
                .cor("Preto")
                .precoUsd(new BigDecimal("10000.00"))
                .ativo(true)
                .build();
    }

    private PageRequestData pageRequest() {
        return new PageRequestData(0, 20, List.of());
    }
}
