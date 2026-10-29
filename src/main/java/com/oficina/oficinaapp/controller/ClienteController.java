package com.oficina.oficinaapp.controller;

import com.oficina.oficinaapp.model.Cliente;
import com.oficina.oficinaapp.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    public static final String SESSAO_CLIENTE = "clienteId";

    public record CadastroRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Size(max = 250) String endereco,
            @NotBlank @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter DDD + número") String telefone,
            @NotBlank @Email @Size(max = 160) String email) {
    }

    public record ReenvioRequest(@NotBlank @Email String email) {
    }

    public record ValidacaoRequest(
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "\\d{6}", message = "O código tem 6 dígitos") String token) {
    }

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody CadastroRequest req) throws Exception {
        Cliente c = new Cliente();
        c.setNome(req.nome().trim());
        c.setEndereco(req.endereco().trim());
        c.setTelefone(req.telefone());
        c.setEmail(req.email());
        service.cadastrar(c);
        return ResponseEntity.ok(Map.of("mensagem", "Código enviado para o seu e-mail."));
    }

    @PostMapping("/reenviar")
    public ResponseEntity<?> reenviar(@Valid @RequestBody ReenvioRequest req) throws Exception {
        boolean enviado = service.reenviar(req.email());
        if (!enviado) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", "Aguarde 1 minuto para pedir um novo código."));
        }
        return ResponseEntity.ok(Map.of("mensagem", "Novo código enviado."));
    }

    @PostMapping("/validar")
    public ResponseEntity<?> validar(@Valid @RequestBody ValidacaoRequest req, HttpSession sessao) throws Exception {
        String clienteId = service.validar(req.email(), req.token());
        if (clienteId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", "Código inválido ou expirado."));
        }
        sessao.setAttribute(SESSAO_CLIENTE, clienteId); // libera o atendimento nesta sessão
        return ResponseEntity.ok(Map.of("mensagem", "Verificado com sucesso!"));
    }
}

