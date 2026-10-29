package com.oficina.oficinaapp.service;

public class ItemInspecao {

    private int numero;          // 0 = linha "Outros"
    private String secao;
    private String descricao;
    private String status;       // OK ou PROBLEMA
    private String obs;

    public ItemInspecao() {}

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getSecao() { return secao; }
    public void setSecao(String secao) { this.secao = secao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
}