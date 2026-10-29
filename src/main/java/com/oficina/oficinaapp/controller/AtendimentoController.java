package com.oficina.oficinaapp.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import com.oficina.oficinaapp.service.Atendimento;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {
    @PostMapping
    public String criarAtendimento(@RequestBody Atendimento atendimento)
        throws ExecutionException, InterruptedException{

        //Regra de negócio: se o pagamento for crédito, marca o acréscimo
        boolean credito = "CREDITO".equalsIgnoreCase(atendimento.getPagamento());
        atendimento.setAcrescimoCredito(credito);

        Firestore db = FirestoreClient.getFirestore();

        DocumentReference docRef = db.collection("atendimentos").document();
        docRef.set(atendimento).get(); //espera a gravação terminar

        return "Atendimento salvo com sucesso! ID: " + docRef.getId();
    }

}
