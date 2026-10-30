package com.oficina.oficinaapp.model;

import com.google.cloud.firestore.annotation.DocumentId;

public class Cliente {

    @DocumentId
    private String id;                 // gerado pelo Firestore

    private String nome;
    private String endereco;
    private String telefone;
    private String email;              // usado para enviar o checklist para assinatura

    private long cadastradoEm;         // milissegundos (epoch)

    public Cliente() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getCadastradoEm() { return cadastradoEm; }
    public void setCadastradoEm(long cadastradoEm) { this.cadastradoEm = cadastradoEm; }
}
