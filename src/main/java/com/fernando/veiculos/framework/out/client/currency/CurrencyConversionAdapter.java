package com.fernando.veiculos.framework.out.client.currency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernando.veiculos.application.port.out.CurrencyConversionPortOut;
import com.fernando.veiculos.domain.exception.CurrencyConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CurrencyConversionAdapter implements CurrencyConversionPortOut {

    private static final Logger log = LoggerFactory.getLogger(CurrencyConversionAdapter.class);

    private final RestClient restClient;
    private final String primaryUrl;
    private final String fallbackUrl;

    public CurrencyConversionAdapter(RestClient.Builder restClientBuilder,
                                     @Value("${app.currency.primary-url}") String primaryUrl,
                                     @Value("${app.currency.fallback-url}") String fallbackUrl) {
        this.restClient = restClientBuilder.build();
        this.primaryUrl = primaryUrl;
        this.fallbackUrl = fallbackUrl;
    }

    @Override
    @Cacheable(cacheNames = "dollarQuote", key = "'USD_BRL'", cacheManager = "redisCacheManager")
    public BigDecimal obterCotacaoUsdParaBrl() {
        try {
            return obterCotacaoAwesomeApi();
        } catch (RuntimeException primaryFailure) {
            log.warn("falha ao consultar cotacao na AwesomeAPI, tentando fallback Frankfurter");
            try {
                return obterCotacaoFrankfurter();
            } catch (RuntimeException fallbackFailure) {
                throw new CurrencyConversionException("nao foi possivel obter cotacao USD-BRL", fallbackFailure);
            }
        }
    }

    private BigDecimal obterCotacaoAwesomeApi() {
        JsonNode response = restClient.get()
                .uri(primaryUrl)
                .retrieve()
                .body(JsonNode.class);

        JsonNode bid = response == null ? null : response.path("USDBRL").path("bid");
        if (bid == null || bid.isMissingNode() || bid.asText().isBlank()) {
            throw new IllegalStateException("resposta invalida da AwesomeAPI");
        }
        return new BigDecimal(bid.asText());
    }

    private BigDecimal obterCotacaoFrankfurter() {
        JsonNode response = restClient.get()
                .uri(fallbackUrl)
                .retrieve()
                .body(JsonNode.class);

        JsonNode brl = response == null ? null : response.path("rates").path("BRL");
        if (brl == null || brl.isMissingNode() || !brl.isNumber()) {
            throw new IllegalStateException("resposta invalida da Frankfurter");
        }
        return brl.decimalValue();
    }
}
