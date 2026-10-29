package com.oficina.oficinaapp.model;
import com.google.cloud.firestore.annotation.DocumentId;

public class Cliente {

    @DocumentId
    private String id;

    private String nome;
    private String endereco;
    private String telefone;
    private String email;

    private boolean verificado;
    private String tokenHash;
    private long tokenExpiraEm; // milissegundos (epoch)
    private int tentativas;

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

    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public long getTokenExpiraEm() { return tokenExpiraEm; }
    public void setTokenExpiraEm(long tokenExpiraEm) { this.tokenExpiraEm = tokenExpiraEm; }

    public int getTentativas() { return tentativas; }
    public void setTentativas(int tentativas) { this.tentativas = tentativas; }
}
