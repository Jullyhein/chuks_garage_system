package com.oficina.oficinaapp.controller;

import com.oficina.oficinaapp.model.Orcamento;
import com.oficina.oficinaapp.service.OrcamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Orcamento orcamento) throws Exception {
        try {
            return ResponseEntity.ok(service.criar(orcamento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public List<Orcamento> listar() throws Exception {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orcamento> buscar(@PathVariable String id) throws Exception {
        Orcamento o = service.buscar(id);
        return o == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(o);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Orcamento orcamento) throws Exception {
        try {
            Orcamento salvo = service.atualizar(id, orcamento);
            return salvo == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
