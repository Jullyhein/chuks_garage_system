package com.oficina.oficinaapp.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.oficina.oficinaapp.model.Cliente;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Cadastro de clientes feito pelo funcionário da oficina (sem verificação por e-mail). */
@Service
public class ClienteService {

    private static final String COLECAO = "clientes";

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    /** Grava um novo cliente e devolve o cadastro com o id gerado. */
    public Cliente cadastrar(Cliente dados) throws Exception {
        normalizar(dados);
        dados.setCadastradoEm(System.currentTimeMillis());

        DocumentReference ref = db().collection(COLECAO).document();
        ref.set(dados).get();
        dados.setId(ref.getId());
        return dados;
    }

    public List<Cliente> listar() throws Exception {
        List<Cliente> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : db().collection(COLECAO).get().get().getDocuments()) {
            lista.add(doc.toObject(Cliente.class));
        }
        return lista;
    }

    /** Devolve null se não existir. */
    public Cliente buscar(String id) throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO).document(id).get().get();
        return doc.exists() ? doc.toObject(Cliente.class) : null;
    }

    /** Atualiza os dados do cliente. Devolve null se não existir. */
    public Cliente atualizar(String id, Cliente dados) throws Exception {
        DocumentReference ref = db().collection(COLECAO).document(id);
        DocumentSnapshot atual = ref.get().get();
        if (!atual.exists()) return null;

        normalizar(dados);
        Long cadastradoEm = atual.getLong("cadastradoEm");
        dados.setCadastradoEm(cadastradoEm == null ? 0 : cadastradoEm);   // a data de cadastro não muda
        ref.set(dados).get();
        dados.setId(id);
        return dados;
    }

    private void normalizar(Cliente c) {
        c.setNome(c.getNome() == null ? null : c.getNome().trim());
        c.setEndereco(c.getEndereco() == null ? null : c.getEndereco().trim());
        c.setEmail(c.getEmail() == null ? null : c.getEmail().trim().toLowerCase());
    }
}
