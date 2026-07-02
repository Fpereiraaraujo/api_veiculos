package com.fernando.veiculos.framework.in.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernando.veiculos.application.port.out.CurrencyConversionPortOut;
import com.fernando.veiculos.framework.out.repository.VeiculoJpaRepository;
import com.fernando.veiculos.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class VeiculoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VeiculoJpaRepository veiculoJpaRepository;

    @MockitoBean
    private CurrencyConversionPortOut currencyConversionPortOut;

    @BeforeEach
    void setUp() {
        veiculoJpaRepository.deleteAll();
        when(currencyConversionPortOut.obterCotacaoUsdParaBrl()).thenReturn(new BigDecimal("5.00"));
    }

    @Test
    void deveExecutarCrudCompleto() {
        HttpHeaders headers = authHeaders("admin", "admin123");

        ResponseEntity<JsonNode> cadastro = restTemplate.postForEntity(
                "/veiculos",
                new HttpEntity<>(veiculoJson("ABC1D23", "Honda", "Civic", 2021, "Preto", "10000.00"), headers),
                JsonNode.class
        );

        assertThat(cadastro.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = cadastro.getBody().path("id").asText();
        assertThat(cadastro.getBody().path("precoBrl").decimalValue()).isEqualByComparingTo("50000.00");

        ResponseEntity<JsonNode> consulta = restTemplate.exchange(
                "/veiculos/" + id,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );

        assertThat(consulta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consulta.getBody().path("placa").asText()).isEqualTo("ABC1D23");

        ResponseEntity<JsonNode> atualizacao = restTemplate.exchange(
                "/veiculos/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(veiculoJson("ABC1D23", "Honda", "Accord", 2022, "Prata", "12000.00"), headers),
                JsonNode.class
        );

        assertThat(atualizacao.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(atualizacao.getBody().path("modelo").asText()).isEqualTo("Accord");
        assertThat(atualizacao.getBody().path("precoBrl").decimalValue()).isEqualByComparingTo("60000.00");

        ResponseEntity<Void> remocao = restTemplate.exchange(
                "/veiculos/" + id,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(remocao.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<JsonNode> aposRemocao = restTemplate.exchange(
                "/veiculos/" + id,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );

        assertThat(aposRemocao.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveFiltrarVeiculosComFiltrosCombinados() {
        HttpHeaders headers = authHeaders("admin", "admin123");
        cadastrar(headers, "AAA1A11", "Honda", "Civic", 2021, "Preto", "10000.00");
        cadastrar(headers, "BBB2B22", "Honda", "Fit", 2021, "Branco", "9000.00");
        cadastrar(headers, "CCC3C33", "Toyota", "Corolla", 2021, "Preto", "11000.00");

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/veiculos?marca=Honda&ano=2021&cor=Preto&minPreco=9000&maxPreco=11000&sort=marca,asc",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders("user", "user123")),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().path("page").path("totalElements").asLong()).isEqualTo(1);
        assertThat(response.getBody().path("content").get(0).path("placa").asText()).isEqualTo("AAA1A11");
    }

    private void cadastrar(HttpHeaders headers, String placa, String marca, String modelo, Integer ano,
                           String cor, String precoUsd) {
        restTemplate.postForEntity(
                "/veiculos",
                new HttpEntity<>(veiculoJson(placa, marca, modelo, ano, cor, precoUsd), headers),
                JsonNode.class
        );
    }

    private HttpHeaders authHeaders(String username, String password) {
        ResponseEntity<JsonNode> login = restTemplate.postForEntity(
                "/auth/login",
                Map.of("username", username, "password", password),
                JsonNode.class
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getBody().path("token").asText());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Object> veiculoJson(String placa, String marca, String modelo, Integer ano,
                                            String cor, String precoUsd) {
        return Map.of(
                "placa", placa,
                "marca", marca,
                "modelo", modelo,
                "ano", ano,
                "cor", cor,
                "precoUsd", new BigDecimal(precoUsd)
        );
    }
}
