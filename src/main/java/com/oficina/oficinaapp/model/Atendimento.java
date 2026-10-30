package com.oficina.oficinaapp.model;

import com.google.cloud.firestore.annotation.DocumentId;

public class Atendimento {

    @DocumentId
    private String id;                 // gerado pelo Firestore

    private String clienteId;          // cliente já cadastrado pela oficina

    // veículo
    private String carro;
    private String marca;
    private int ano;

    // atendimento
    private String reclamacao;
    private String pagamento;          // ex.: PIX, DINHEIRO, DEBITO, CREDITO
    private boolean acrescimoCredito;  // calculado pelo sistema a partir do pagamento
    private String dataAgendamento;    // formato 2026-10-21

    public Atendimento() {}            // o Firestore exige construtor vazio

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getCarro() { return carro; }
    public void setCarro(String carro) { this.carro = carro; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public String getReclamacao() { return reclamacao; }
    public void setReclamacao(String reclamacao) { this.reclamacao = reclamacao; }

    public String getPagamento() { return pagamento; }
    public void setPagamento(String pagamento) { this.pagamento = pagamento; }

    public boolean isAcrescimoCredito() { return acrescimoCredito; }
    public void setAcrescimoCredito(boolean acrescimoCredito) { this.acrescimoCredito = acrescimoCredito; }

    public String getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(String dataAgendamento) { this.dataAgendamento = dataAgendamento; }
}
