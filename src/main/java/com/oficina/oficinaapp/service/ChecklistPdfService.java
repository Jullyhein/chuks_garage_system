package com.oficina.oficinaapp.service;

import com.oficina.oficinaapp.model.Checklist;
import com.oficina.oficinaapp.model.ItemInspecao;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ChecklistPdfService {

    private static final Logger log = LoggerFactory.getLogger(ChecklistPdfService.class);

    // mesma pasta usada pelo ChecklistController (relativa ao diretório de execução)
    private static final Path PASTA_UPLOADS = Paths.get("uploads").toAbsolutePath().normalize();

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, Color.BLACK);
    private static final Font F_SUB = FontFactory.getFont(FontFactory.HELVETICA, 13, Font.BOLD, Color.BLACK);
    private static final Font F_SECAO = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, new Color(192, 57, 43));
    private static final Font F_ROTULO = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
    private static final Font F_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font F_OBS = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
    private static final Font F_OK = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, new Color(30, 130, 76));
    private static final Font F_PROBLEMA = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, new Color(192, 57, 43));

    public byte[] gerar(Checklist c) throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, saida);
            doc.open();

            adicionarLogo(doc);

            Paragraph titulo = new Paragraph("Checklist do Veículo", F_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(14);
            doc.add(titulo);

            doc.add(tabelaDados(c));
            adicionarInspecao(doc, c);
            adicionarFotos(doc, c);
            adicionarAssinatura(doc, c);
        } finally {
            if (doc.isOpen()) doc.close();
        }
        return saida.toByteArray();
    }

    // ---------------------------------------------------------------
    private void adicionarLogo(Document doc) {
        try (InputStream in = new ClassPathResource("static/images/chuks_logo.png").getInputStream()) {
            Image logo = Image.getInstance(in.readAllBytes());
            logo.scaleToFit(150, 70);
            logo.setAlignment(Element.ALIGN_CENTER);
            doc.add(logo);
        } catch (Exception e) {
            log.warn("PDF sem logo (confira src/main/resources/static/images/chuks_logo.png): {}", e.toString());
        }
    }

    private PdfPTable tabelaDados(Checklist c) {
        String condicao = c.isVeioDeGuincho() ? "Veio de guincho"
                : c.isEmFuncionamento() ? "Em funcionamento"
                : "Não informado";

        PdfPTable t = new PdfPTable(new float[]{35, 65});
        t.setWidthPercentage(100);
        linha(t, "Cliente", c.getNomeCliente());
        linha(t, "Endereço", c.getEndereco());
        linha(t, "E-mail", c.getEmail());
        linha(t, "Telefone", c.getTelefone());
        linha(t, "Data da inspeção", formatarData(c.getDataInspecao()));
        linha(t, "Veículo", c.getVeiculo());
        linha(t, "Placa", c.getPlaca());
        linha(t, "KM (odômetro)", c.getKmOdometro() == null ? "" : c.getKmOdometro() + " km");
        linha(t, "Nível de gasolina", c.getNivelGasolina());
        linha(t, "Condição do veículo", condicao);
        return t;
    }

    private void adicionarInspecao(Document doc, Checklist c) throws Exception {
        if (c.getItens() == null || c.getItens().isEmpty()) return;

        Paragraph h = new Paragraph("Inspeção visual", F_SUB);
        h.setSpacingBefore(16);
        doc.add(h);

        String secaoAtual = null;
        PdfPTable tabela = null;

        for (ItemInspecao i : c.getItens()) {
            if (i == null) continue;
            String secao = nvl(i.getSecao());

            if (tabela == null || !secao.equals(secaoAtual)) {
                if (tabela != null) doc.add(tabela);
                secaoAtual = secao;

                Paragraph s = new Paragraph(secao.toUpperCase(), F_SECAO);
                s.setSpacingBefore(10);
                s.setSpacingAfter(3);
                doc.add(s);

                tabela = new PdfPTable(new float[]{82, 18});
                tabela.setWidthPercentage(100);
            }

            boolean ok = "OK".equals(i.getStatus());

            Phrase descricao = new Phrase();
            descricao.add(new Chunk((i.getNumero() > 0 ? i.getNumero() + ". " : "") + nvl(i.getDescricao()), F_TEXTO));
            if (i.getObs() != null && !i.getObs().isBlank()) {
                descricao.add(new Chunk("\nObs.: " + i.getObs(), F_OBS));
            }

            PdfPCell celDescricao = new PdfPCell(descricao);
            celDescricao.setPadding(4);

            PdfPCell celStatus = new PdfPCell(new Phrase(ok ? "OK" : "Problema", ok ? F_OK : F_PROBLEMA));
            celStatus.setPadding(4);
            celStatus.setHorizontalAlignment(Element.ALIGN_CENTER);

            tabela.addCell(celDescricao);
            tabela.addCell(celStatus);
        }
        if (tabela != null) doc.add(tabela);
    }

    private void adicionarFotos(Document doc, Checklist c) throws Exception {
        if (c.getFotos() == null || c.getFotos().isEmpty()) return;

        Paragraph h = new Paragraph("Fotos do veículo", F_SUB);
        h.setSpacingBefore(16);
        h.setSpacingAfter(6);
        doc.add(h);

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        int adicionadas = 0;

        for (String url : c.getFotos()) {
            try {
                Image img = Image.getInstance(reduzir(resolverCaminho(url)));
                PdfPCell cel = new PdfPCell(img, true);
                cel.setBorder(Rectangle.NO_BORDER);
                cel.setPadding(4);
                t.addCell(cel);
                adicionadas++;
            } catch (Exception e) {
                // mostra o motivo real (arquivo não encontrado, formato não suportado como HEIC, etc.)
                log.warn("Foto ignorada no PDF ({}): {}", url, e.toString());
            }
        }

        if (adicionadas % 2 == 1) {          // completa a última linha
            PdfPCell vazia = new PdfPCell(new Phrase(""));
            vazia.setBorder(Rectangle.NO_BORDER);
            t.addCell(vazia);
        }
        if (adicionadas > 0) doc.add(t);
    }

    private void adicionarAssinatura(Document doc, Checklist c) throws Exception {
        Paragraph texto = new Paragraph(
                "Declaro que conferi as informações acima sobre o estado do meu veículo na entrada na oficina.",
                F_TEXTO);
        texto.setSpacingBefore(26);
        texto.setKeepTogether(true);
        doc.add(texto);

        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(60);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.setSpacingBefore(10);
        t.setKeepTogether(true);

        // espaço onde o cliente posiciona a assinatura do gov.br
        PdfPCell area = new PdfPCell(new Phrase(""));
        area.setMinimumHeight(70);
        area.setBorder(Rectangle.BOTTOM);
        t.addCell(area);

        PdfPCell nome = new PdfPCell(new Phrase(nvl(c.getNomeCliente()) + " (cliente)", F_ROTULO));
        nome.setBorder(Rectangle.NO_BORDER);
        nome.setHorizontalAlignment(Element.ALIGN_CENTER);
        nome.setPadding(4);
        t.addCell(nome);

        doc.add(t);
    }

    // ---------------------------------------------------------------
    private void linha(PdfPTable t, String rotulo, String valor) {
        t.addCell(celula(rotulo, F_ROTULO));
        t.addCell(celula(valor, F_TEXTO));
    }

    private PdfPCell celula(String texto, Font fonte) {
        PdfPCell cel = new PdfPCell(new Phrase(nvl(texto), fonte));
        cel.setPadding(5);
        return cel;
    }

    // "/uploads/checklists/ID/arquivo.jpg" -> caminho absoluto no disco, sem sair da pasta uploads
    private Path resolverCaminho(String url) {
        String relativo = url.startsWith("/") ? url.substring(1) : url;
        Path arquivo = Paths.get("").toAbsolutePath().resolve(relativo).normalize();
        if (!arquivo.startsWith(PASTA_UPLOADS)) {
            throw new IllegalArgumentException("Caminho fora da pasta de uploads");
        }
        if (!Files.isRegularFile(arquivo)) {
            throw new IllegalArgumentException("Arquivo não encontrado: " + arquivo);
        }
        return arquivo;
    }

    private byte[] reduzir(Path arquivo) throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Thumbnails.of(arquivo.toFile())
                .size(800, 800)
                .imageType(BufferedImage.TYPE_INT_RGB)   // evita fundo preto em PNG transparente
                .outputFormat("jpg")
                .outputQuality(0.75)
                .toOutputStream(saida);
        return saida.toByteArray();
    }

    private String formatarData(String iso) {
        if (iso == null || !iso.matches("\\d{4}-\\d{2}-\\d{2}")) return nvl(iso);
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}