package com.oficina.oficinaapp;

public class Orcamento {

    private String id;

    // identificação do veículo
    private String chassi;
    private String modelo;
    private Integer ano;
    private String cor;
    private String marca;
    private String placa;
    private String prisma;            // nº do prisma
    private Integer km;

    // datas
    private String dataChegada;
    private String dataAberturaOS;

    // serviço
    private String reclamacoesCliente;
    private String tipoServico;
    private String revisao;
    private String diagnostico;
    private String garantia;
    private String observacaoCliente;
    private String servicoAFazer;
    private String responsavelTecnico;
    private String problemasEnfrentados;

    public Orcamento() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChassi() { return chassi; }
    public void setChassi(String chassi) { this.chassi = chassi; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getPrisma() { return prisma; }
    public void setPrisma(String prisma) { this.prisma = prisma; }

    public Integer getKm() { return km; }
    public void setKm(Integer km) { this.km = km; }

    public String getDataChegada() { return dataChegada; }
    public void setDataChegada(String dataChegada) { this.dataChegada = dataChegada; }

    public String getDataAberturaOS() { return dataAberturaOS; }
    public void setDataAberturaOS(String dataAberturaOS) { this.dataAberturaOS = dataAberturaOS; }

    public String getReclamacoesCliente() { return reclamacoesCliente; }
    public void setReclamacoesCliente(String v) { this.reclamacoesCliente = v; }

    public String getTipoServico() { return tipoServico; }
    public void setTipoServico(String tipoServico) { this.tipoServico = tipoServico; }

    public String getRevisao() { return revisao; }
    public void setRevisao(String revisao) { this.revisao = revisao; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getGarantia() { return garantia; }
    public void setGarantia(String garantia) { this.garantia = garantia; }

    public String getObservacaoCliente() { return observacaoCliente; }
    public void setObservacaoCliente(String v) { this.observacaoCliente = v; }

    public String getServicoAFazer() { return servicoAFazer; }
    public void setServicoAFazer(String servicoAFazer) { this.servicoAFazer = servicoAFazer; }

    public String getResponsavelTecnico() { return responsavelTecnico; }
    public void setResponsavelTecnico(String v) { this.responsavelTecnico = v; }

    public String getProblemasEnfrentados() { return problemasEnfrentados; }
    public void setProblemasEnfrentados(String v) { this.problemasEnfrentados = v; }
}