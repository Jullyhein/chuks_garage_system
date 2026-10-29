package com.oficina.oficinaapp.service;

public class Atendimento {

    private String nome;
    private String carro;
    private String marca;
    private int ano;
    private String reclamacao;
    private boolean acrescimoCredito;
    private String pagamento;
    private String dataAgendamento;
    private String email;

    //Getters e Setters


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCarro() {
        return carro;
    }

    public void setCarro(String carro) {
        this.carro = carro;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getReclamacao() {
        return reclamacao;
    }

    public void setReclamacao(String reclamacao) {
        this.reclamacao = reclamacao;
    }

    public boolean isAcrescimoCredito() {
        return acrescimoCredito;
    }

    public void setAcrescimoCredito(boolean acrescimoCredito) {
        this.acrescimoCredito = acrescimoCredito;
    }

    public String getPagamento() {
        return pagamento;
    }

    public void setPagamento(String pagamento) {
        this.pagamento = pagamento;
    }

    public String getDataAgendamento() {
        return dataAgendamento;
    }
    public void setDataAgendamento(String dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
