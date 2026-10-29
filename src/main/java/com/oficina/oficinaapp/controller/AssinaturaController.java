package com.oficina.oficinaapp.controller;

import com.oficina.oficinaapp.service.AssinaturaService;
import com.oficina.oficinaapp.service.Checklist;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/assinaturas")
public class AssinaturaController {

    private final AssinaturaService servico;

    public AssinaturaController(AssinaturaService servico) {
        this.servico = servico;
    }

    // dados mínimos para a página do cliente
    @GetMapping("/{token}")
    public ResponseEntity<?> consultar(@PathVariable String token) throws Exception {
        Checklist c = servico.buscarPorToken(token);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "nome", nvl(c.getNomeCliente()),
                "veiculo", nvl(c.getVeiculo()),
                "placa", nvl(c.getPlaca()),
                "assinado", AssinaturaService.ASSINADO.equals(c.getStatusAssinatura())));
    }

    // o cliente pode baixar o PDF de novo, caso perca o anexo do e-mail
    @GetMapping("/{token}/pdf")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable String token) throws Exception {
        Checklist c = servico.buscarPorToken(token);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"checklist.pdf\"")
                .body(servico.gerarPdf(c));
    }

    // recebe o PDF assinado
    @PostMapping("/{token}")
    public ResponseEntity<?> enviarAssinado(@PathVariable String token,
                                            @RequestParam("arquivo") MultipartFile arquivo) throws Exception {
        Checklist c = servico.buscarPorToken(token);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            servico.receberAssinado(c, arquivo);
            return ResponseEntity.ok(Map.of("assinado", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}