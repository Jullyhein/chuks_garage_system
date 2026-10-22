package com.oficina.oficinaapp;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private static final String COLECAO = "orcamentos";

    private static final Pattern CHASSI = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    private static final String MSG_CHASSI =
            "O chassi deve ter exatamente 17 caracteres (letras e números, sem I, O e Q).";

    // limpa o valor (espaços, maiúsculas) e confere o formato
    private boolean chassiValido(Orcamento orcamento) {
        String chassi = orcamento.getChassi() == null ? "" : orcamento.getChassi().trim().toUpperCase();
        orcamento.setChassi(chassi);
        return CHASSI.matcher(chassi).matches();
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Orcamento orcamento) throws Exception {
        if (!chassiValido(orcamento)) {
            return ResponseEntity.badRequest().body(Map.of("erro", MSG_CHASSI));
        }
        DocumentReference ref = db().collection(COLECAO).document();
        orcamento.setId(ref.getId());
        ref.set(orcamento).get();
        return ResponseEntity.ok(orcamento);
    }

    @GetMapping
    public List<Orcamento> listar() throws Exception {
        List<Orcamento> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : db().collection(COLECAO).get().get().getDocuments()) {
            lista.add(doc.toObject(Orcamento.class));
        }
        return lista;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orcamento> buscar(@PathVariable String id) throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO).document(id).get().get();
        if (!doc.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(doc.toObject(Orcamento.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id,
                                       @RequestBody Orcamento orcamento) throws Exception {
        if (!chassiValido(orcamento)) {
            return ResponseEntity.badRequest().body(Map.of("erro", MSG_CHASSI));
        }
        orcamento.setId(id);
        db().collection(COLECAO).document(id).set(orcamento).get();
        return ResponseEntity.ok(orcamento);
    }
}