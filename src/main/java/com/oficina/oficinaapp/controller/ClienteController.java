package com.oficina.oficinaapp.controller;

import com.oficina.oficinaapp.model.Cliente;
import com.oficina.oficinaapp.service.ClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    // o e-mail é obrigatório porque é para ele que o checklist vai para assinatura
    public record ClienteRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Size(max = 250) String endereco,
            @NotBlank @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter DDD + número") String telefone,
            @NotBlank @Email @Size(max = 160) String email) {

        Cliente paraCliente() {
            Cliente c = new Cliente();
            c.setNome(nome);
            c.setEndereco(endereco);
            c.setTelefone(telefone);
            c.setEmail(email);
            return c;
        }
    }

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public Cliente cadastrar(@Valid @RequestBody ClienteRequest req) throws Exception {
        return service.cadastrar(req.paraCliente());
    }

    @GetMapping
    public List<Cliente> listar() throws Exception {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscar(@PathVariable String id) throws Exception {
        Cliente c = service.buscar(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable String id,
                                             @Valid @RequestBody ClienteRequest req) throws Exception {
        Cliente c = service.atualizar(id, req.paraCliente());
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }
}
