package com.oficina.oficinaapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.oficina.oficinaapp.model.Atendimento;
import com.oficina.oficinaapp.model.Checklist;
import com.oficina.oficinaapp.model.Cliente;
import com.oficina.oficinaapp.model.ItemInspecao;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChecklistService {

    private static final int MIN_FOTOS = 2;
    private static final int MAX_FOTOS = 11;
    private static final String COLECAO_CHECKLISTS = "checklists";

    /** Resultado do salvamento: o checklist gravado e se o e-mail de assinatura saiu. */
    public record Salvo(Checklist checklist, boolean emailEnviado) {}

    private final ObjectMapper mapper = new ObjectMapper();
    private final AssinaturaService assinaturaService;
    private final AtendimentoService atendimentoService;
    private final ClienteService clienteService;

    public ChecklistService(AssinaturaService assinaturaService,
                            AtendimentoService atendimentoService,
                            ClienteService clienteService) {
        this.assinaturaService = assinaturaService;
        this.atendimentoService = atendimentoService;
        this.clienteService = clienteService;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    /**
     * Dados para pré-preencher o checklist a partir do atendimento (veículo) e do cliente cadastrado.
     * Devolve null se o atendimento não existir.
     */
    public Map<String, Object> dadosCliente(String atendimentoId) throws Exception {
        Atendimento a = atendimentoService.buscar(atendimentoId);
        if (a == null) return null;

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("atendimentoId", atendimentoId);
        dados.put("clienteId", a.getClienteId());
        dados.put("veiculo", String.join(" ",
                nvl(a.getMarca()), nvl(a.getCarro()), a.getAno() > 0 ? String.valueOf(a.getAno()) : "").trim());

        Cliente c = a.getClienteId() == null ? null : clienteService.buscar(a.getClienteId());
        if (c != null) {
            dados.put("nomeCliente", nvl(c.getNome()));
            dados.put("endereco", nvl(c.getEndereco()));
            dados.put("email", nvl(c.getEmail()));
            dados.put("telefone", nvl(c.getTelefone()));
        }
        return dados;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    /**
     * Valida, grava o checklist + fotos e envia a cópia ao cliente.
     * Regras inválidas lançam IllegalArgumentException (o controller transforma em 400).
     */
    public Salvo salvar(Checklist checklist, MultipartFile[] fotos, String itensJson) throws Exception {
        int quantidade = (fotos == null) ? 0 : fotos.length;
        if (quantidade < MIN_FOTOS || quantidade > MAX_FOTOS) {
            throw new IllegalArgumentException(
                    "Envie entre " + MIN_FOTOS + " e " + MAX_FOTOS + " fotos (recebidas: " + quantidade + ").");
        }

        checklist.setItens(lerItens(itensJson));

        if (checklist.isEmFuncionamento() && checklist.isVeioDeGuincho()) {
            throw new IllegalArgumentException(
                    "Escolha apenas uma condição: em funcionamento ou veio de guincho.");
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
        return new Salvo(checklist, emailEnviado);
    }

    public List<Checklist> listar() throws Exception {
        List<Checklist> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : db().collection(COLECAO_CHECKLISTS).get().get().getDocuments()) {
            lista.add(doc.toObject(Checklist.class));
        }
        return lista;
    }

    /** Devolve null se não existir. */
    public Checklist buscar(String id) throws Exception {
        DocumentSnapshot doc = db().collection(COLECAO_CHECKLISTS).document(id).get().get();
        return doc.exists() ? doc.toObject(Checklist.class) : null;
    }

    /** Reenvia o e-mail de um checklist salvo. Devolve false se o checklist não existir. */
    public boolean reenviarEmail(String id) throws Exception {
        Checklist c = buscar(id);
        if (c == null) return false;
        assinaturaService.reenviar(c);       // IllegalArgumentException se já assinado
        return true;
    }

    // ------------------------------------------------------------------
    private List<ItemInspecao> lerItens(String itensJson) {
        if (itensJson == null || itensJson.isBlank()) {
            throw new IllegalArgumentException("A inspeção visual é obrigatória.");
        }
        List<ItemInspecao> itens;
        try {
            itens = mapper.readValue(itensJson, new TypeReference<List<ItemInspecao>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Inspeção visual em formato inválido.");
        }
        for (ItemInspecao item : itens) {
            if (!"OK".equals(item.getStatus()) && !"PROBLEMA".equals(item.getStatus())) {
                throw new IllegalArgumentException("Status inválido na inspeção (item " + item.getNumero() + ").");
            }
        }
        return itens;
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
            String original = foto.getOriginalFilename() == null ? "foto" : foto.getOriginalFilename();
            String nome = UUID.randomUUID() + "_" + Paths.get(original).getFileName();
            Files.copy(foto.getInputStream(), pasta.resolve(nome));
            urls.add("/uploads/checklists/" + checklistId + "/" + nome);
        }
        return urls;
    }
}
