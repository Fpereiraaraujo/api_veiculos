package com.fernando.veiculos.framework.in.controller;

import com.fernando.veiculos.application.model.PageRequestData;
import com.fernando.veiculos.application.model.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.veiculos.application.port.in.VeiculoPortIn;
import com.fernando.veiculos.domain.exception.DuplicatePlacaException;
import com.fernando.veiculos.framework.in.exceptionhandler.GlobalExceptionHandler;
import com.fernando.veiculos.framework.in.mapper.VeiculoHttpMapper;
import com.fernando.veiculos.infrastructure.config.AuthUsersConfig;
import com.fernando.veiculos.infrastructure.config.SecurityConfig;
import com.fernando.veiculos.infrastructure.security.JwtAuthenticationFilter;
import com.fernando.veiculos.infrastructure.security.JwtService;
import com.fernando.veiculos.infrastructure.security.RestAccessDeniedHandler;
import com.fernando.veiculos.infrastructure.security.RestAuthenticationEntryPoint;
import com.fernando.veiculos.infrastructure.security.SecurityErrorWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VeiculoController.class)
@Import({
        SecurityConfig.class,
        AuthUsersConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        SecurityErrorWriter.class,
        VeiculoHttpMapper.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = "app.jwt.secret=teste-segredo-com-tamanho-suficiente-para-hmac-sha512-0123456789")
class VeiculoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VeiculoPortIn veiculoPortIn;

    @Test
    void deveRetornar401QuandoListagemNaoTemToken() throws Exception {
        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/veiculos"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403QuandoUsuarioTentaCadastrar() throws Exception {
        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(veiculoJson("ABC1D23")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "USER")
    void devePermitirListagemParaUsuario() throws Exception {
        when(veiculoPortIn.buscar(isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequestData.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar409QuandoPlacaJaExiste() throws Exception {
        when(veiculoPortIn.cadastrar(any())).thenThrow(new DuplicatePlacaException("ABC1D23"));

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(veiculoJson("ABC1D23")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("ja existe um veiculo cadastrado com a placa: ABC1D23"));
    }

    private String veiculoJson(String placa) throws JsonProcessingException {
        return objectMapper.writeValueAsString(Map.of(
                "placa", placa,
                "marca", "Honda",
                "modelo", "Civic",
                "ano", 2021,
                "cor", "Preto",
                "precoUsd", new BigDecimal("10000.00")
        ));
    }
}
