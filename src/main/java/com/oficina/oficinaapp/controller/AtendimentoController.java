package com.oficina.oficinaapp.controller;

import com.oficina.oficinaapp.model.Atendimento;
import com.oficina.oficinaapp.service.AtendimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {

    private final AtendimentoService service;

    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> criarAtendimento(@RequestBody Atendimento atendimento) throws Exception {
        try {
            String id = service.criar(atendimento);
            return ResponseEntity.ok("Atendimento salvo com sucesso! ID: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public List<Atendimento> listar() throws Exception {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Atendimento> buscar(@PathVariable String id) throws Exception {
        Atendimento a = service.buscar(id);
        return a == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(a);
    }
}
