package com.oficina.oficinaapp.service;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class Checklist {

    private String id;
    private String atendimentoId;

    // dados do cliente
    private String nomeCliente;
    private String endereco;
    private String email;
    private String telefone;

    // dados do veículo
    private String veiculo;
    private String placa;
    //private String numeroCarro;
    private Integer kmOdometro;
    private String nivelGasolina;
    private String dataInspecao;          // formato 2026-10-21

    // condições gerais
    private boolean emFuncionamento;
    private boolean veioDeGuincho;

    // assinatura (gov.br)
    private String statusAssinatura;        // PENDENTE ou ASSINADO
    private String tokenAssinatura;         // segredo do link do cliente
    private Long assinaturaSolicitadaEm;    // milissegundos
    private Long ultimoLembreteEm;
    private int lembretesEnviados;
    private Long assinadoEm;
    private String arquivoAssinado;

    // inspeção visual e fotos
    private List<ItemInspecao> itens = new ArrayList<>();
    private List<String> fotos = new ArrayList<>();

    public Checklist() {}   // o Firestore exige construtor vazio

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAtendimentoId() { return atendimentoId; }
    public void setAtendimentoId(String atendimentoId) { this.atendimentoId = atendimentoId; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getVeiculo() { return veiculo; }
    public void setVeiculo(String veiculo) { this.veiculo = veiculo; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

//    public String getNumeroCarro() { return numeroCarro; }
//    public void setNumeroCarro(String numeroCarro) { this.numeroCarro = numeroCarro; }

    public Integer getKmOdometro() { return kmOdometro; }
    public void setKmOdometro(Integer kmOdometro) { this.kmOdometro = kmOdometro; }

    public String getNivelGasolina() { return nivelGasolina; }
    public void setNivelGasolina(String nivelGasolina) { this.nivelGasolina = nivelGasolina; }

    public String getDataInspecao() { return dataInspecao; }
    public void setDataInspecao(String dataInspecao) { this.dataInspecao = dataInspecao; }

    public boolean isEmFuncionamento() { return emFuncionamento; }
    public void setEmFuncionamento(boolean emFuncionamento) { this.emFuncionamento = emFuncionamento; }

    public boolean isVeioDeGuincho() { return veioDeGuincho; }
    public void setVeioDeGuincho(boolean veioDeGuincho) { this.veioDeGuincho = veioDeGuincho; }

    public List<ItemInspecao> getItens() { return itens; }
    public void setItens(List<ItemInspecao> itens) { this.itens = itens; }

    public List<String> getFotos() { return fotos; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }

    public String getStatusAssinatura() { return statusAssinatura; }
    public void setStatusAssinatura(String statusAssinatura) { this.statusAssinatura = statusAssinatura; }

    @JsonIgnore
    public String getTokenAssinatura() { return tokenAssinatura; }
    public void setTokenAssinatura(String tokenAssinatura) { this.tokenAssinatura = tokenAssinatura; }

    public Long getAssinaturaSolicitadaEm() { return assinaturaSolicitadaEm; }
    public void setAssinaturaSolicitadaEm(Long assinaturaSolicitadaEm) { this.assinaturaSolicitadaEm = assinaturaSolicitadaEm; }

    public Long getUltimoLembreteEm() { return ultimoLembreteEm; }
    public void setUltimoLembreteEm(Long ultimoLembreteEm) { this.ultimoLembreteEm = ultimoLembreteEm; }

    public int getLembretesEnviados() { return lembretesEnviados; }
    public void setLembretesEnviados(int lembretesEnviados) { this.lembretesEnviados = lembretesEnviados; }

    public Long getAssinadoEm() { return assinadoEm; }
    public void setAssinadoEm(Long assinadoEm) { this.assinadoEm = assinadoEm; }

    @JsonIgnore
    public String getArquivoAssinado() { return arquivoAssinado; }
    public void setArquivoAssinado(String arquivoAssinado) { this.arquivoAssinado = arquivoAssinado; }
}