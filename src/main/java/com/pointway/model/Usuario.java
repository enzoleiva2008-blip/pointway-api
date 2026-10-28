package com.pointway.model;

import java.time.LocalDate;

public class Usuario {

    public static final int TAXA_PONTOS_POR_REAL = 100;
    public static final int CONVERSAO_MINIMA_PONTOS = 500;
    public static final int LIMITE_RESGATES_24H = 3;

    private int id;
    private String nome;
    private String cpf;
    private String email;
    private int saldoPontos;
    private LocalDate dataCadastro;

    public Usuario() {
    }

    public Usuario(String nome, String cpf, String email, int saldoPontos) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.saldoPontos = saldoPontos;
        this.dataCadastro = LocalDate.now();
    }

    public Usuario(int id, String nome, String cpf, String email, int saldoPontos, LocalDate dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.saldoPontos = saldoPontos;
        this.dataCadastro = dataCadastro;
    }

    /**
     * Converte pontos SoulUp em crédito de transporte (taxa: 100 pontos = R$ 1,00).
     * Regra de negócio do Desafio SoulTransporte: conversão mínima de 500 pontos.
     */
    public double converterPontosEmCredito(int pontosDesejados) {
        if (pontosDesejados < CONVERSAO_MINIMA_PONTOS) {
            throw new IllegalArgumentException(
                    "A conversao minima e de " + CONVERSAO_MINIMA_PONTOS + " pontos.");
        }
        if (pontosDesejados > saldoPontos) {
            throw new IllegalStateException(
                    "Saldo insuficiente. Saldo atual: " + saldoPontos + " pontos.");
        }
        this.saldoPontos -= pontosDesejados;
        return pontosDesejados / (double) TAXA_PONTOS_POR_REAL;
    }

    /**
     * Regra de prevenção de fraude: limita resgates por CPF em uma janela de 24h.
     */
    public boolean podeResgatar(int resgatesUltimas24h) {
        return resgatesUltimas24h < LIMITE_RESGATES_24H;
    }

    public void adicionarPontos(int pontos) {
        if (pontos <= 0) {
            throw new IllegalArgumentException("A quantidade de pontos deve ser positiva.");
        }
        this.saldoPontos += pontos;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getSaldoPontos() {
        return saldoPontos;
    }

    public void setSaldoPontos(int saldoPontos) {
        this.saldoPontos = saldoPontos;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nome='" + nome + "', cpf='" + cpf + "', saldoPontos=" + saldoPontos + "}";
    }
}
