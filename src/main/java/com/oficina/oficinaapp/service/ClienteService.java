package com.oficina.oficinaapp.service;

import com.oficina.oficinaapp.model.Cliente;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class ClienteService {

    private static final String COLECAO = "clientes";
    private static final long VALIDADE_MS = 10 * 60 * 1000L;   // token vale 10 minutos
    private static final long REENVIO_MIN_MS = 60 * 1000L;     // espera mínima entre envios
    private static final int MAX_TENTATIVAS = 5;

    private final Firestore db;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public ClienteService(Firestore db, EmailService emailService) {
        this.db = db;
        this.emailService = emailService;
    }

    /** Salva (ou atualiza) o cliente como NÃO verificado e envia o token por e-mail. */
    public void cadastrar(Cliente dados) throws Exception {
        String email = normalizar(dados.getEmail());
        dados.setEmail(email);
        dados.setVerificado(false);

        String token = gerarToken();
        aplicarToken(dados, token);

        db.collection(COLECAO).document(idDe(email)).set(dados).get();
        emailService.enviarToken(email, dados.getNome(), token);
    }

    /** Gera um novo token para um cadastro existente (respeitando o intervalo mínimo). */
    public boolean reenviar(String emailInformado) throws Exception {
        String email = normalizar(emailInformado);
        DocumentReference ref = db.collection(COLECAO).document(idDe(email));
        DocumentSnapshot snap = ref.get().get();
        if (!snap.exists()) return false;

        Cliente c = snap.toObject(Cliente.class);
        if (c == null || c.isVerificado()) return false;

        // se o token atual foi enviado há menos de 1 minuto, não envia outro
        long enviadoEm = c.getTokenExpiraEm() - VALIDADE_MS;
        if (System.currentTimeMillis() - enviadoEm < REENVIO_MIN_MS) return false;

        String token = gerarToken();
        aplicarToken(c, token);
        ref.update("tokenHash", c.getTokenHash(),
                "tokenExpiraEm", c.getTokenExpiraEm(),
                "tentativas", 0).get();
        emailService.enviarToken(email, c.getNome(), token);
        return true;
    }

    /** Confere o token. Retorna o id do cliente se estiver correto; senão, null. */
    public String validar(String emailInformado, String tokenInformado) throws Exception {
        String email = normalizar(emailInformado);
        DocumentReference ref = db.collection(COLECAO).document(idDe(email));
        DocumentSnapshot snap = ref.get().get();
        if (!snap.exists()) return null;

        Cliente c = snap.toObject(Cliente.class);
        if (c == null || c.getTokenHash() == null) return null;
        if (System.currentTimeMillis() > c.getTokenExpiraEm()) return null;
        if (c.getTentativas() >= MAX_TENTATIVAS) return null;

        boolean confere = MessageDigest.isEqual(
                hash(tokenInformado.trim()).getBytes(StandardCharsets.UTF_8),
                c.getTokenHash().getBytes(StandardCharsets.UTF_8));

        if (!confere) {
            ref.update("tentativas", FieldValue.increment(1)).get();
            return null;
        }

        ref.update("verificado", true,
                "tokenHash", FieldValue.delete(),
                "tokenExpiraEm", 0,
                "tentativas", 0).get();
        return ref.getId();
    }

    // ---------- auxiliares ----------

    private void aplicarToken(Cliente c, String token) {
        c.setTokenHash(hash(token));
        c.setTokenExpiraEm(System.currentTimeMillis() + VALIDADE_MS);
        c.setTentativas(0);
    }

    private String gerarToken() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }

    /** O id do documento é o hash do e-mail: garante um cadastro por e-mail. */
    private String idDe(String email) {
        return hash(email);
    }

    private String hash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
