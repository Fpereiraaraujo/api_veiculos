package com.fernando.veiculos.framework.out.client.currency;

import com.fernando.veiculos.domain.exception.CurrencyConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CurrencyConversionAdapterTest {

    private static final String PRIMARY_URL = "https://currency.test/primary";
    private static final String FALLBACK_URL = "https://currency.test/fallback";

    private MockRestServiceServer server;
    private CacheManager localCacheManager;
    private CacheManager distributedCacheManager;
    private CurrencyConversionAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        localCacheManager = new ConcurrentMapCacheManager("dollarQuote");
        distributedCacheManager = new ConcurrentMapCacheManager("dollarQuote");
        adapter = new CurrencyConversionAdapter(builder, PRIMARY_URL, FALLBACK_URL, localCacheManager, distributedCacheManager);
    }

    @Test
    void deveConsultarApiPrimariaEGravarCache() {
        server.expect(once(), requestTo(PRIMARY_URL))
                .andRespond(withSuccess("{\"USDBRL\":{\"bid\":\"5.1234\"}}", MediaType.APPLICATION_JSON));

        BigDecimal cotacao = adapter.obterCotacaoUsdParaBrl();

        assertThat(cotacao).isEqualByComparingTo("5.1234");
        assertThat(localCacheManager.getCache("dollarQuote").get("USD_BRL", BigDecimal.class))
                .isEqualByComparingTo("5.1234");
        assertThat(distributedCacheManager.getCache("dollarQuote").get("USD_BRL", BigDecimal.class))
                .isEqualByComparingTo("5.1234");
        server.verify();
    }

    @Test
    void deveUsarFallbackQuandoApiPrimariaFalhar() {
        server.expect(once(), requestTo(PRIMARY_URL)).andRespond(withServerError());
        server.expect(once(), requestTo(FALLBACK_URL))
                .andRespond(withSuccess("{\"rates\":{\"BRL\":5.55}}", MediaType.APPLICATION_JSON));

        BigDecimal cotacao = adapter.obterCotacaoUsdParaBrl();

        assertThat(cotacao).isEqualByComparingTo("5.55");
        server.verify();
    }

    @Test
    void deveRetornarCacheSemChamarApiQuandoCotacaoExiste() {
        localCacheManager.getCache("dollarQuote").put("USD_BRL", new BigDecimal("5.77"));

        BigDecimal cotacao = adapter.obterCotacaoUsdParaBrl();

        assertThat(cotacao).isEqualByComparingTo("5.77");
        server.verify();
    }

    @Test
    void deveAquecerCacheLocalQuandoValorExisteSomenteNoRedis() {
        distributedCacheManager.getCache("dollarQuote").put("USD_BRL", new BigDecimal("5.88"));

        BigDecimal cotacao = adapter.obterCotacaoUsdParaBrl();

        assertThat(cotacao).isEqualByComparingTo("5.88");
        assertThat(localCacheManager.getCache("dollarQuote").get("USD_BRL", BigDecimal.class))
                .isEqualByComparingTo("5.88");
        server.verify();
    }

    @Test
    void deveLancarExcecaoQuandoApisFalhamESemCache() {
        server.expect(once(), requestTo(PRIMARY_URL)).andRespond(withServerError());
        server.expect(once(), requestTo(FALLBACK_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.obterCotacaoUsdParaBrl())
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessage("nao foi possivel obter cotacao USD-BRL");
        server.verify();
    }
}
