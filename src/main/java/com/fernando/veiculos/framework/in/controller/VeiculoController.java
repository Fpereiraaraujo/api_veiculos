package com.fernando.veiculos.framework.in.controller;

import com.fernando.veiculos.application.port.in.VeiculoPortIn;
import com.fernando.veiculos.domain.exception.BusinessException;
import com.fernando.veiculos.domain.model.Veiculo;
import com.fernando.veiculos.framework.in.dto.RelatorioPorMarcaDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoPatchRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoPortIn veiculoPortIn;

    public VeiculoController(VeiculoPortIn veiculoPortIn) {
        this.veiculoPortIn = veiculoPortIn;
    }

    @GetMapping
    public Page<VeiculoResponseDTO> buscar(@RequestParam(required = false) String marca,
                                           @RequestParam(required = false) Integer ano,
                                           @RequestParam(required = false) String cor,
                                           @RequestParam(required = false) BigDecimal minPreco,
                                           @RequestParam(required = false) BigDecimal maxPreco,
                                           @PageableDefault(size = 20, sort = "marca") Pageable pageable) {
        return veiculoPortIn.buscar(marca, ano, cor, minPreco, maxPreco, pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    public VeiculoResponseDTO buscarPorId(@PathVariable UUID id) {
        return toResponse(veiculoPortIn.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> cadastrar(@Valid @RequestBody VeiculoRequestDTO request) {
        Veiculo criado = veiculoPortIn.cadastrar(toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(criado));
    }

    @PutMapping("/{id}")
    public VeiculoResponseDTO atualizar(@PathVariable UUID id,
                                        @Valid @RequestBody VeiculoRequestDTO request) {
        return toResponse(veiculoPortIn.atualizar(id, toDomain(request)));
    }

    @PatchMapping("/{id}")
    public VeiculoResponseDTO atualizarParcial(@PathVariable UUID id,
                                               @Valid @RequestBody VeiculoPatchRequestDTO request) {
        if (request.vazio()) {
            throw new BusinessException("informe ao menos um campo para atualizar");
        }
        return toResponse(veiculoPortIn.atualizarParcial(id, toDomain(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        veiculoPortIn.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorios/por-marca")
    public List<RelatorioPorMarcaDTO> relatorioPorMarca() {
        return veiculoPortIn.relatorioPorMarca().stream()
                .map(item -> new RelatorioPorMarcaDTO(item.marca(), item.quantidade()))
                .toList();
    }

    private Veiculo toDomain(VeiculoRequestDTO request) {
        return Veiculo.builder()
                .placa(request.placa())
                .marca(request.marca())
                .modelo(request.modelo())
                .ano(request.ano())
                .cor(request.cor())
                .precoUsd(request.precoUsd())
                .build();
    }

    private Veiculo toDomain(VeiculoPatchRequestDTO request) {
        return Veiculo.builder()
                .placa(request.placa())
                .marca(request.marca())
                .modelo(request.modelo())
                .ano(request.ano())
                .cor(request.cor())
                .precoUsd(request.precoUsd())
                .build();
    }

    private VeiculoResponseDTO toResponse(Veiculo veiculo) {
        return new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPrecoUsd(),
                veiculo.isAtivo(),
                veiculo.getCreatedAt(),
                veiculo.getUpdatedAt()
        );
    }
}
