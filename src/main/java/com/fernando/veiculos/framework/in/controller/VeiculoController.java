package com.fernando.veiculos.framework.in.controller;

import com.fernando.veiculos.application.port.in.VeiculoPortIn;
import com.fernando.veiculos.domain.model.Veiculo;
import com.fernando.veiculos.framework.in.dto.RelatorioPorMarcaDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoPatchRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoRequestDTO;
import com.fernando.veiculos.framework.in.dto.VeiculoResponseDTO;
import com.fernando.veiculos.framework.in.mapper.VeiculoHttpMapper;
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
    private final VeiculoHttpMapper mapper;

    public VeiculoController(VeiculoPortIn veiculoPortIn, VeiculoHttpMapper mapper) {
        this.veiculoPortIn = veiculoPortIn;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<VeiculoResponseDTO> buscar(@RequestParam(required = false) String marca,
                                           @RequestParam(required = false) Integer ano,
                                           @RequestParam(required = false) String cor,
                                           @RequestParam(required = false) BigDecimal minPreco,
                                           @RequestParam(required = false) BigDecimal maxPreco,
                                           @PageableDefault(size = 20, sort = "marca") Pageable pageable) {
        return veiculoPortIn.buscar(marca, ano, cor, minPreco, maxPreco, pageable).map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public VeiculoResponseDTO buscarPorId(@PathVariable UUID id) {
        return mapper.toResponse(veiculoPortIn.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> cadastrar(@Valid @RequestBody VeiculoRequestDTO request) {
        Veiculo criado = veiculoPortIn.cadastrar(mapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    public VeiculoResponseDTO atualizar(@PathVariable UUID id,
                                        @Valid @RequestBody VeiculoRequestDTO request) {
        return mapper.toResponse(veiculoPortIn.atualizar(id, mapper.toDomain(request)));
    }

    @PatchMapping("/{id}")
    public VeiculoResponseDTO atualizarParcial(@PathVariable UUID id,
                                               @Valid @RequestBody VeiculoPatchRequestDTO request) {
        return mapper.toResponse(veiculoPortIn.atualizarParcial(id, mapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        veiculoPortIn.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorios/por-marca")
    public List<RelatorioPorMarcaDTO> relatorioPorMarca() {
        return veiculoPortIn.relatorioPorMarca().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
