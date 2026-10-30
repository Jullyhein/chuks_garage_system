package com.oficina.oficinaapp.controller;

import com.oficina.oficinaapp.model.Checklist;
import com.oficina.oficinaapp.service.ChecklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    private final ChecklistService service;

    public ChecklistController(ChecklistService service) {
        this.service = service;
    }

    // campos que o cliente do navegador NÃO pode preencher
    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.setDisallowedFields("fotos", "itens", "id",
                "statusAssinatura", "tokenAssinatura", "assinaturaSolicitadaEm",
                "ultimoLembreteEm", "lembretesEnviados", "assinadoEm", "arquivoAssinado");
    }

    // Tela inicial: busca os dados do cliente para preencher o checklist
    @GetMapping("/cliente/{atendimentoId}")
    public ResponseEntity<Map<String, Object>> dadosCliente(@PathVariable String atendimentoId) throws Exception {
        Map<String, Object> dados = service.dadosCliente(atendimentoId);
        return dados == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dados);
    }

    // Salva o checklist + fotos (multipart/form-data) e envia a cópia ao cliente
    @PostMapping
    public ResponseEntity<?> salvar(@ModelAttribute Checklist checklist,
                                    @RequestParam(value = "fotos", required = false) MultipartFile[] fotos,
                                    @RequestParam(value = "itensJson", required = false) String itensJson)
            throws Exception {
        try {
            ChecklistService.Salvo salvo = service.salvar(checklist, fotos, itensJson);
            return ResponseEntity.ok(Map.of("checklist", salvo.checklist(), "emailEnviado", salvo.emailEnviado()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public List<Checklist> listar() throws Exception {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Checklist> buscar(@PathVariable String id) throws Exception {
        Checklist c = service.buscar(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    // Reenvia o e-mail de um checklist já salvo
    @PostMapping("/{id}/reenviar-email")
    public ResponseEntity<?> reenviarEmail(@PathVariable String id) throws Exception {
        try {
            return service.reenviarEmail(id)
                    ? ResponseEntity.ok(Map.of("emailEnviado", true))
                    : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao enviar o e-mail."));
        }
    }
}
