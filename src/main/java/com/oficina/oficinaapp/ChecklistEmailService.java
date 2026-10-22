package com.oficina.oficinaapp;

import jakarta.mail.internet.MimeMessage;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChecklistEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public ChecklistEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ------------------------------------------------------------------
    // Envio
    // ------------------------------------------------------------------
    public void enviar(Checklist c) throws Exception {
        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

        helper.setFrom(remetente, "Chuk's Car Services");
        helper.setTo(c.getEmail());
        helper.setSubject("Checklist do seu veículo - Chuk's Car Services");

        // 1) fotos reduzidas; as que derem erro são ignoradas
        List<byte[]> fotos = new ArrayList<>();
        if (c.getFotos() != null) {
            for (String url : c.getFotos()) {
                try {
                    // url é "/uploads/checklists/ID/arquivo.jpg" -> caminho no disco
                    Path arquivo = Paths.get(url.substring(1));
                    fotos.add(reduzir(arquivo));
                } catch (Exception e) {
                    System.err.println("Foto ignorada no e-mail (" + url + "): " + e.getMessage());
                }
            }
        }

        // 2) o texto HTML precisa ser definido ANTES das imagens inline
        helper.setText(montarHtml(c, fotos.size()), true);

        helper.addInline("logo", new ClassPathResource("static/images/chuks_logo.png"), "image/png");
        for (int i = 0; i < fotos.size(); i++) {
            helper.addInline("foto" + i, new ByteArrayResource(fotos.get(i)), "image/jpeg");
        }

        mailSender.send(mensagem);
    }

    // ------------------------------------------------------------------
    // Redução das fotos (900 px, JPEG, corrige a rotação de celular)
    // ------------------------------------------------------------------
    private byte[] reduzir(Path arquivo) throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Thumbnails.of(arquivo.toFile())
                .size(900, 900)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toOutputStream(saida);
        return saida.toByteArray();
    }

    // ------------------------------------------------------------------
    // Corpo do e-mail
    // ------------------------------------------------------------------
    private String montarHtml(Checklist c, int qtdFotos) {
        String condicao = c.isVeioDeGuincho() ? "Veio de guincho"
                : c.isEmFuncionamento() ? "Em funcionamento"
                : "Não informado";

        StringBuilder h = new StringBuilder();
        h.append("<div style='background:#1a1a1a;padding:24px;font-family:Arial,sans-serif;color:#e0e0e0;'>");
        h.append("<div style='max-width:600px;margin:0 auto;background:#242424;border-radius:12px;padding:24px;'>");

        h.append("<img src='cid:logo' alt=\"Chuk's Car Services\" style='display:block;max-width:200px;margin:0 auto 20px;'>");
        h.append("<h2 style='color:#ffffff;text-align:center;margin:0 0 8px;'>Checklist do seu veículo</h2>");
        h.append("<p style='text-align:center;color:#cccccc;margin:0 0 24px;'>")
                .append("Olá, ").append(esc(c.getNomeCliente()))
                .append("! Segue a cópia do registro de entrada do seu carro na oficina.</p>");

        // dados gerais
        h.append("<table style='width:100%;border-collapse:collapse;'>");
        linha(h, "Cliente", c.getNomeCliente());
        linha(h, "Endereço", c.getEndereco());
        linha(h, "E-mail", c.getEmail());
        linha(h, "Telefone", c.getTelefone());
        linha(h, "Data da inspeção", formatarData(c.getDataInspecao()));
        linha(h, "Veículo", c.getVeiculo());
        linha(h, "Placa", c.getPlaca());
        linha(h, "Nº do carro", c.getNumeroCarro());
        linha(h, "KM (odômetro)", c.getKmOdometro() == null ? "" : c.getKmOdometro() + " km");
        linha(h, "Nível de gasolina", c.getNivelGasolina());
        linha(h, "Condição do veículo", condicao);
        h.append("</table>");

        // inspeção visual
        h.append(montarInspecao(c));

        // fotos, duas por linha
        if (qtdFotos > 0) {
            h.append("<h3 style='color:#ffffff;margin:24px 0 12px;'>Fotos do veículo</h3>");
            h.append("<table style='width:100%;border-collapse:collapse;'>");
            for (int i = 0; i < qtdFotos; i += 2) {
                h.append("<tr>");
                for (int j = i; j < i + 2; j++) {
                    h.append("<td style='width:50%;padding:4px;vertical-align:top;'>");
                    if (j < qtdFotos) {
                        h.append("<img src='cid:foto").append(j)
                                .append("' style='width:100%;border-radius:6px;display:block;'>");
                    }
                    h.append("</td>");
                }
                h.append("</tr>");
            }
            h.append("</table>");
        }

        h.append("<p style='color:#888;font-size:12px;text-align:center;margin-top:24px;'>")
                .append("Este é um e-mail automático, apenas para consulta.</p>");
        h.append("</div></div>");
        return h.toString();
    }

    // ------------------------------------------------------------------
    // Inspeção visual agrupada por seção
    // ------------------------------------------------------------------
    private String montarInspecao(Checklist c) {
        if (c.getItens() == null || c.getItens().isEmpty()) {
            return "";
        }

        StringBuilder h = new StringBuilder();
        h.append("<h3 style='color:#ffffff;margin:24px 0 8px;'>Inspeção visual</h3>");

        String secaoAtual = null;
        for (ItemInspecao i : c.getItens()) {

            // começou uma nova seção: fecha a tabela anterior e abre outra
            if (secaoAtual == null || !secaoAtual.equals(i.getSecao())) {
                if (secaoAtual != null) {
                    h.append("</table>");
                }
                secaoAtual = i.getSecao();
                h.append("<p style='color:#c0392b;font-weight:bold;text-transform:uppercase;margin:16px 0 4px;'>")
                        .append(esc(secaoAtual)).append("</p>")
                        .append("<table style='width:100%;border-collapse:collapse;'>");
            }

            boolean ok = "OK".equals(i.getStatus());
            String numero = i.getNumero() > 0 ? i.getNumero() + ". " : "";

            h.append("<tr>")
                    .append("<td style='padding:6px 8px;border-bottom:1px solid #444;color:#e0e0e0;'>")
                    .append(numero).append(esc(i.getDescricao()));

            if (i.getObs() != null && !i.getObs().isBlank()) {
                h.append("<br><span style='color:#aaaaaa;font-size:12px;'>Obs.: ")
                        .append(esc(i.getObs())).append("</span>");
            }

            h.append("</td>")
                    .append("<td style='padding:6px 8px;border-bottom:1px solid #444;width:90px;")
                    .append("text-align:right;font-weight:bold;color:")
                    .append(ok ? "#2ecc71" : "#e74c3c").append(";'>")
                    .append(ok ? "OK" : "Problema")
                    .append("</td></tr>");
        }
        h.append("</table>");
        return h.toString();
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------
    private void linha(StringBuilder h, String rotulo, String valor) {
        h.append("<tr>")
                .append("<td style='padding:8px;border-bottom:1px solid #444;color:#aaaaaa;width:40%;'>")
                .append(rotulo).append("</td>")
                .append("<td style='padding:8px;border-bottom:1px solid #444;color:#ffffff;'>")
                .append(esc(valor)).append("</td></tr>");
    }

    // 2026-10-21 -> 21/10/2026 (se não estiver nesse formato, devolve como veio)
    private String formatarData(String iso) {
        if (iso == null || !iso.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return iso;
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
    }

    // evita que texto digitado vire HTML dentro do e-mail
    private String esc(String texto) {
        return texto == null ? "" : HtmlUtils.htmlEscape(texto);
    }
}