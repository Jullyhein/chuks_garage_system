package com.oficina.oficinaapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    private static final int MIN_FOTOS = 2;
    private static final int MAX_FOTOS = 11;
    private static final String COLECAO_ATENDIMENTOS = "atendimentos";
    private static final String COLECAO_CHECKLISTS = "checklists";

    private final ObjectMapper mapper = new ObjectMapper();
    private final AssinaturaService assinaturaService;

    public ChecklistController(AssinaturaService assinaturaService) {
        this.assinaturaService = assinaturaService;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.setDisallowedFields("fotos", "itens", "id",
                "statusAssinatura", "tokenAssinatura", "assinaturaSolicitadaEm",
                "ultimoLembreteEm", "lembretesEnviados", "assinadoEm", "arquivoAssinado");
    }

    // Tela inicial: busca os dados do cliente para preencher o checklist
    @GetMapping("/cliente/{atendimentoId}")
    public ResponseEntity<Map<String, Object>> dadosCliente(@PathVariable String atendimentoId)
            throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO_ATENDIMENTOS)
                .document(atendimentoId).get().get();
        if (!doc.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(doc.getData());
    }

    // Salva o checklist + fotos (multipart/form-data) e envia a cópia ao cliente
    @PostMapping
    public ResponseEntity<?> salvar(@ModelAttribute Checklist checklist,
                                    @RequestParam(value = "fotos", required = false) MultipartFile[] fotos,
                                    @RequestParam(value = "itensJson", required = false) String itensJson)
            throws Exception {

        int quantidade = (fotos == null) ? 0 : fotos.length;
        if (quantidade < MIN_FOTOS || quantidade > MAX_FOTOS) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Envie entre " + MIN_FOTOS + " e " + MAX_FOTOS + " fotos (recebidas: " + quantidade + ")."));
        }

        // inspeção visual
        if (itensJson == null || itensJson.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A inspeção visual é obrigatória."));
        }
        List<ItemInspecao> itens = mapper.readValue(itensJson, new TypeReference<List<ItemInspecao>>() {});
        for (ItemInspecao item : itens) {
            if (!"OK".equals(item.getStatus()) && !"PROBLEMA".equals(item.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("erro",
                        "Status inválido na inspeção (item " + item.getNumero() + ")."));
            }
        }
        checklist.setItens(itens);

        if (checklist.isEmFuncionamento() && checklist.isVeioDeGuincho()) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Escolha apenas uma condição: em funcionamento ou veio de guincho."));
        }

        DocumentReference ref = db().collection(COLECAO_CHECKLISTS).document();
        checklist.setId(ref.getId());
        checklist.setFotos(salvarFotos(ref.getId(), fotos));

        ref.set(checklist).get();

        // envia a cópia ao cliente; se falhar, o checklist já está salvo
        boolean emailEnviado = false;
        if (checklist.getEmail() != null && !checklist.getEmail().isBlank()) {
            try {
                assinaturaService.solicitar(checklist);   // gera token, envia PDF e e-mail
                emailEnviado = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(Map.of("checklist", checklist, "emailEnviado", emailEnviado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Checklist> buscar(@PathVariable String id) throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO_CHECKLISTS).document(id).get().get();
        if (!doc.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(doc.toObject(Checklist.class));
    }

    // Reenvia o e-mail de um checklist já salvo
    @PostMapping("/{id}/reenviar-email")
    public ResponseEntity<?> reenviarEmail(@PathVariable String id) throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO_CHECKLISTS).document(id).get().get();
        if (!doc.exists()) {
            return ResponseEntity.notFound().build();
        }
        try {
            assinaturaService.reenviar(doc.toObject(Checklist.class));
            return ResponseEntity.ok(Map.of("emailEnviado", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao enviar o e-mail."));
        }
    }

    private List<String> salvarFotos(String checklistId, MultipartFile[] fotos) throws IOException {
        List<String> urls = new ArrayList<>();
        if (fotos == null) return urls;

        Path pasta = Paths.get("uploads", "checklists", checklistId);
        Files.createDirectories(pasta);

        for (MultipartFile foto : fotos) {
            if (foto.isEmpty()) continue;
            String tipo = foto.getContentType();
            if (tipo == null || !tipo.startsWith("image/")) {
                throw new IllegalArgumentException("Só são aceitas imagens.");
            }
            String nome = UUID.randomUUID() + "_" + Paths.get(foto.getOriginalFilename()).getFileName();
            Files.copy(foto.getInputStream(), pasta.resolve(nome));
            urls.add("/uploads/checklists/" + checklistId + "/" + nome);
        }
        return urls;
    }
}