package com.oficina.oficinaapp.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssinaturaService {

    public static final String PENDENTE = "PENDENTE";
    public static final String ASSINADO = "ASSINADO";

    private static final String COLECAO = "checklists";
    private static final Path PASTA_ASSINADOS = Paths.get("documentos-assinados");   // fora de /uploads (não é público)
    private static final long UM_DIA = 24L * 60 * 60 * 1000;

    private final ChecklistPdfService pdfService;
    private final ChecklistEmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${assinatura.lembrete.intervalo-dias:2}")
    private int intervaloDias;

    @Value("${assinatura.lembrete.maximo:3}")
    private int maxLembretes;

    public AssinaturaService(ChecklistPdfService pdfService, ChecklistEmailService emailService) {
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    // ------------------------------------------------------------------
    // Pedido de assinatura (chamado logo depois de salvar o checklist)
    // ------------------------------------------------------------------
    public void solicitar(Checklist c) throws Exception {
        long agora = System.currentTimeMillis();

        c.setTokenAssinatura(gerarToken());
        c.setStatusAssinatura(PENDENTE);
        c.setAssinaturaSolicitadaEm(agora);
        c.setLembretesEnviados(0);

        Map<String, Object> campos = new HashMap<>();
        campos.put("tokenAssinatura", c.getTokenAssinatura());
        campos.put("statusAssinatura", PENDENTE);
        campos.put("assinaturaSolicitadaEm", agora);
        campos.put("lembretesEnviados", 0);
        db().collection(COLECAO).document(c.getId()).update(campos).get();

        // se o envio falhar, o status já está PENDENTE e o lembrete tenta de novo depois
        emailService.enviar(c, pdfService.gerar(c), linkAssinatura(c));
    }

    // Reenvio manual (usado pelo endpoint reenviar-email)
    public void reenviar(Checklist c) throws Exception {
        if (c.getTokenAssinatura() == null) {       // checklist antigo, sem pedido de assinatura
            solicitar(c);
            return;
        }
        if (ASSINADO.equals(c.getStatusAssinatura())) {
            throw new IllegalArgumentException("Este checklist já foi assinado.");
        }
        emailService.enviar(c, pdfService.gerar(c), linkAssinatura(c));
    }

    public byte[] gerarPdf(Checklist c) throws Exception {
        return pdfService.gerar(c);
    }

    public String linkAssinatura(Checklist c) {
        return baseUrl + "/assinar.html?token=" + c.getTokenAssinatura();
    }

    // ------------------------------------------------------------------
    // Consulta pelo token do link
    // ------------------------------------------------------------------
    public Checklist buscarPorToken(String token) throws Exception {
        if (token == null || token.length() < 20 || token.length() > 100) {
            return null;
        }
        List<QueryDocumentSnapshot> docs = db().collection(COLECAO)
                .whereEqualTo("tokenAssinatura", token)
                .limit(1)
                .get().get().getDocuments();
        return docs.isEmpty() ? null : docs.get(0).toObject(Checklist.class);
    }

    // ------------------------------------------------------------------
    // Recebimento do PDF assinado
    // ------------------------------------------------------------------
    public void receberAssinado(Checklist c, MultipartFile arquivo) throws Exception {
        if (ASSINADO.equals(c.getStatusAssinatura())) {
            throw new IllegalArgumentException("Este checklist já foi assinado.");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie o arquivo PDF assinado.");
        }

        byte[] bytes = arquivo.getBytes();
        String texto = new String(bytes, StandardCharsets.ISO_8859_1);

        if (!texto.startsWith("%PDF")) {
            throw new IllegalArgumentException("O arquivo precisa ser um PDF.");
        }
        // PDFs com assinatura digital trazem o marcador /ByteRange
        if (!texto.contains("/ByteRange")) {
            throw new IllegalArgumentException(
                    "Este PDF não parece ter assinatura digital. Assine em assinador.iti.br e envie o arquivo assinado.");
        }

        Files.createDirectories(PASTA_ASSINADOS);
        Path destino = PASTA_ASSINADOS.resolve(c.getId() + ".pdf");
        Files.write(destino, bytes);

        Map<String, Object> campos = new HashMap<>();
        campos.put("statusAssinatura", ASSINADO);
        campos.put("assinadoEm", System.currentTimeMillis());
        campos.put("arquivoAssinado", destino.toString());
        db().collection(COLECAO).document(c.getId()).update(campos).get();
    }

    // ------------------------------------------------------------------
    // Lembretes: roda todo dia às 9h (horário de Brasília)
    // ------------------------------------------------------------------
    @Scheduled(cron = "${assinatura.lembrete.cron:0 0 9 * * *}", zone = "America/Sao_Paulo")
    public void enviarLembretes() {
        try {
            long agora = System.currentTimeMillis();

            for (QueryDocumentSnapshot doc : db().collection(COLECAO)
                    .whereEqualTo("statusAssinatura", PENDENTE)
                    .get().get().getDocuments()) {

                Checklist c = doc.toObject(Checklist.class);

                if (c.getEmail() == null || c.getEmail().isBlank()) continue;
                if (c.getLembretesEnviados() >= maxLembretes) continue;

                long ultimo = c.getUltimoLembreteEm() != null ? c.getUltimoLembreteEm()
                        : (c.getAssinaturaSolicitadaEm() != null ? c.getAssinaturaSolicitadaEm() : agora);
                if (agora - ultimo < intervaloDias * UM_DIA) continue;

                try {
                    emailService.enviarLembrete(c, pdfService.gerar(c), linkAssinatura(c));

                    Map<String, Object> campos = new HashMap<>();
                    campos.put("lembretesEnviados", c.getLembretesEnviados() + 1);
                    campos.put("ultimoLembreteEm", agora);
                    doc.getReference().update(campos).get();
                } catch (Exception e) {
                    System.err.println("Falha no lembrete do checklist " + c.getId());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    private String gerarToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}