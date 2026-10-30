package com.oficina.oficinaapp.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.oficina.oficinaapp.model.Orcamento;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class OrcamentoService {

    private static final String COLECAO = "orcamentos";

    private static final Pattern CHASSI = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    private static final String MSG_CHASSI =
            "O chassi deve ter exatamente 17 caracteres (letras e números, sem I, O e Q).";

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    /** Limpa o chassi (espaços, maiúsculas) e confere o formato; lança IllegalArgumentException se inválido. */
    private void validarChassi(Orcamento orcamento) {
        String chassi = orcamento.getChassi() == null ? "" : orcamento.getChassi().trim().toUpperCase();
        orcamento.setChassi(chassi);
        if (!CHASSI.matcher(chassi).matches()) {
            throw new IllegalArgumentException(MSG_CHASSI);
        }
    }

    public Orcamento criar(Orcamento orcamento) throws Exception {
        validarChassi(orcamento);
        DocumentReference ref = db().collection(COLECAO).document();
        orcamento.setId(ref.getId());
        ref.set(orcamento).get();
        return orcamento;
    }

    public List<Orcamento> listar() throws Exception {
        List<Orcamento> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : db().collection(COLECAO).get().get().getDocuments()) {
            lista.add(doc.toObject(Orcamento.class));
        }
        return lista;
    }

    /** Devolve null se não existir. */
    public Orcamento buscar(String id) throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO).document(id).get().get();
        return doc.exists() ? doc.toObject(Orcamento.class) : null;
    }

    /** Devolve null se o orçamento não existir (não cria documentos com id inventado). */
    public Orcamento atualizar(String id, Orcamento orcamento) throws Exception {
        validarChassi(orcamento);
        DocumentReference ref = db().collection(COLECAO).document(id);
        if (!ref.get().get().exists()) {
            return null;
        }
        orcamento.setId(id);
        ref.set(orcamento).get();
        return orcamento;
    }
}
