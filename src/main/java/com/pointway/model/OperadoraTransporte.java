package com.pointway.model;

public class OperadoraTransporte {

    private int id;
    private String nome;
    private String cidade;
    private String sistemaBilhetagem;

    public OperadoraTransporte() {
    }

    public OperadoraTransporte(String nome, String cidade, String sistemaBilhetagem) {
        this.nome = nome;
        this.cidade = cidade;
        this.sistemaBilhetagem = sistemaBilhetagem;
    }

    public OperadoraTransporte(int id, String nome, String cidade, String sistemaBilhetagem) {
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.sistemaBilhetagem = sistemaBilhetagem;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getSistemaBilhetagem() {
        return sistemaBilhetagem;
    }

    public void setSistemaBilhetagem(String sistemaBilhetagem) {
        this.sistemaBilhetagem = sistemaBilhetagem;
    }

    @Override
    public String toString() {
        return "OperadoraTransporte{id=" + id + ", nome='" + nome + "', cidade='" + cidade + "'}";
    }
}
