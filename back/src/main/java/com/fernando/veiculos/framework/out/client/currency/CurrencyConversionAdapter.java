package com.fernando.veiculos.framework.out.client.currency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernando.veiculos.application.port.out.CurrencyConversionPortOut;
import com.fernando.veiculos.domain.exception.CurrencyConversionException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CurrencyConversionAdapter implements CurrencyConversionPortOut {

    private static final Logger log = LoggerFactory.getLogger(CurrencyConversionAdapter.class);
    private static final String CACHE_NAME = "dollarQuote";
    private static final String CACHE_KEY = "USD_BRL";

    private final RestClient restClient;
    private final String primaryUrl;
    private final String fallbackUrl;
    private final CacheManager localCacheManager;
    private final CacheManager distributedCacheManager;

    public CurrencyConversionAdapter(RestClient.Builder restClientBuilder,
                                     @Value("${app.currency.primary-url}") String primaryUrl,
                                     @Value("${app.currency.fallback-url}") String fallbackUrl,
                                     @Qualifier("caffeineCacheManager") CacheManager localCacheManager,
                                     @Qualifier("redisCacheManager") CacheManager distributedCacheManager) {
        this.restClient = restClientBuilder.build();
        this.primaryUrl = primaryUrl;
        this.fallbackUrl = fallbackUrl;
        this.localCacheManager = localCacheManager;
        this.distributedCacheManager = distributedCacheManager;
    }

    @Override
    @Retry(name = "currencyApi")
    @CircuitBreaker(name = "currencyApi")
    public BigDecimal obterCotacaoUsdParaBrl() {
        BigDecimal cotacaoCache = buscarCotacaoNoCache();
        if (cotacaoCache != null) {
            return cotacaoCache;
        }

        try {
            return salvarNoCache(obterCotacaoAwesomeApi());
        } catch (RuntimeException primaryFailure) {
            log.warn("falha ao consultar cotacao na AwesomeAPI, tentando fallback Frankfurter: {}",
                    primaryFailure.getMessage());
            log.debug("detalhes da falha na AwesomeAPI", primaryFailure);
            try {
                return salvarNoCache(obterCotacaoFrankfurter());
            } catch (RuntimeException fallbackFailure) {
                cotacaoCache = buscarCotacaoNoCache();
                if (cotacaoCache != null) {
                    log.warn("falha ao consultar APIs de cotacao, retornando valor do cache: {}",
                            fallbackFailure.getMessage());
                    log.debug("detalhes da falha nas APIs de cotacao", fallbackFailure);
                    return cotacaoCache;
                }
                throw new CurrencyConversionException("nao foi possivel obter cotacao USD-BRL", fallbackFailure);
            }
        }
    }

    private BigDecimal obterCotacaoAwesomeApi() {
        JsonNode response = consultar(primaryUrl);
        JsonNode bid = response == null ? null : response.path("USDBRL").path("bid");
        if (bid == null || bid.isMissingNode() || bid.asText().isBlank()) {
            throw new IllegalStateException("resposta invalida da AwesomeAPI");
        }
        return new BigDecimal(bid.asText());
    }

    private BigDecimal obterCotacaoFrankfurter() {
        JsonNode response = consultar(fallbackUrl);
        JsonNode brl = response == null ? null : response.path("rates").path("BRL");
        if (brl == null || brl.isMissingNode() || !brl.isNumber()) {
            throw new IllegalStateException("resposta invalida da Frankfurter");
        }
        return brl.decimalValue();
    }

    private JsonNode consultar(String url) {
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);
    }

    private BigDecimal buscarCotacaoNoCache() {
        try {
            BigDecimal cotacaoLocal = buscarCotacao(localCacheManager);
            if (cotacaoLocal != null) {
                return cotacaoLocal;
            }

            BigDecimal cotacaoDistribuida = buscarCotacao(distributedCacheManager);
            if (cotacaoDistribuida != null) {
                salvarNoCacheLocal(cotacaoDistribuida);
            }
            return cotacaoDistribuida;
        } catch (RuntimeException ex) {
            log.warn("falha ao consultar cache de cotacao", ex);
            return null;
        }
    }

    private BigDecimal salvarNoCache(BigDecimal cotacao) {
        try {
            salvarNoCacheLocal(cotacao);
            salvarNoCacheDistribuido(cotacao);
        } catch (RuntimeException ex) {
            log.warn("falha ao gravar cache de cotacao", ex);
        }
        return cotacao;
    }

    private BigDecimal buscarCotacao(CacheManager cacheManager) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        return cache == null ? null : cache.get(CACHE_KEY, BigDecimal.class);
    }

    private void salvarNoCacheLocal(BigDecimal cotacao) {
        Cache cache = localCacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(CACHE_KEY, cotacao);
        }
    }

    private void salvarNoCacheDistribuido(BigDecimal cotacao) {
        Cache cache = distributedCacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(CACHE_KEY, cotacao);
        }
    }
}
