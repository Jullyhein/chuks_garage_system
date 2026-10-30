package com.oficina.oficinaapp.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.oficina.oficinaapp.model.Atendimento;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Atendimento aberto pelo funcionário da oficina para um cliente já cadastrado. */
@Service
public class AtendimentoService {

    private static final String COLECAO = "atendimentos";

    private final ClienteService clienteService;

    public AtendimentoService(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    /** Salva o atendimento e devolve o id gerado. IllegalArgumentException se o cliente não existir. */
    public String criar(Atendimento atendimento) throws Exception {
        String clienteId = atendimento.getClienteId();
        if (clienteId == null || clienteId.isBlank() || clienteService.buscar(clienteId) == null) {
            throw new IllegalArgumentException("Cliente não encontrado. Cadastre o cliente antes do atendimento.");
        }

        // Regra de negócio: se o pagamento for crédito, marca o acréscimo
        boolean credito = "CREDITO".equalsIgnoreCase(atendimento.getPagamento());
        atendimento.setAcrescimoCredito(credito);

        DocumentReference ref = db().collection(COLECAO).document();
        ref.set(atendimento).get();              // espera a gravação terminar
        return ref.getId();
    }

    public List<Atendimento> listar() throws Exception {
        List<Atendimento> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : db().collection(COLECAO).get().get().getDocuments()) {
            lista.add(doc.toObject(Atendimento.class));
        }
        return lista;
    }

    /** Devolve null se não existir. */
    public Atendimento buscar(String id) throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO).document(id).get().get();
        return doc.exists() ? doc.toObject(Atendimento.class) : null;
    }
}
