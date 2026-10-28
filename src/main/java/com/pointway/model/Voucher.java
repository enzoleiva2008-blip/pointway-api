package com.pointway.model;

import java.time.LocalDate;
import java.util.UUID;

public class Voucher {

    public static final int VALIDADE_DIAS = 30;

    private int id;
    private String codigo;
    private Usuario usuario;
    private OperadoraTransporte operadora;
    private double valorCredito;
    private int pontosUtilizados;
    private LocalDate dataEmissao;
    private LocalDate dataValidade;
    private StatusVoucher status;

    public Voucher() {
    }

    public Voucher(String codigo, Usuario usuario, OperadoraTransporte operadora, double valorCredito,
            int pontosUtilizados, LocalDate dataEmissao, LocalDate dataValidade, StatusVoucher status) {
        this.codigo = codigo;
        this.usuario = usuario;
        this.operadora = operadora;
        this.valorCredito = valorCredito;
        this.pontosUtilizados = pontosUtilizados;
        this.dataEmissao = dataEmissao;
        this.dataValidade = dataValidade;
        this.status = status;
    }

    /**
     * Fabrica um voucher a partir da conversao de pontos do usuario (regra de negocio central do Pointway).
     * Codigo unico via UUID; validade de 30 dias a partir da emissao.
     */
    public static Voucher emitir(Usuario usuario, OperadoraTransporte operadora, int pontosDesejados) {
        double valorCredito = usuario.converterPontosEmCredito(pontosDesejados);
        LocalDate emissao = LocalDate.now();

        Voucher voucher = new Voucher();
        voucher.codigo = UUID.randomUUID().toString();
        voucher.usuario = usuario;
        voucher.operadora = operadora;
        voucher.valorCredito = valorCredito;
        voucher.pontosUtilizados = pontosDesejados;
        voucher.dataEmissao = emissao;
        voucher.dataValidade = emissao.plusDays(VALIDADE_DIAS);
        voucher.status = StatusVoucher.ATIVO;
        return voucher;
    }

    public boolean estaValido() {
        return status == StatusVoucher.ATIVO && !LocalDate.now().isAfter(dataValidade);
    }

    /**
     * Calcula o CO2 economizado (kg) com base no modal utilizado e na distancia percorrida.
     */
    public double calcularCO2Economizado(ModalTransporte modal, double distanciaKm) {
        if (distanciaKm < 0) {
            throw new IllegalArgumentException("A distancia nao pode ser negativa.");
        }
        return distanciaKm * modal.getFatorReducaoCO2KgPorKm();
    }

    /**
     * Valida o uso do voucher na catraca: exige voucher ativo e dentro da validade,
     * registra a viagem e consome o voucher (uso unico).
     */
    public Viagem validarNaCatraca(ModalTransporte modal, double distanciaKm) {
        if (!estaValido()) {
            throw new IllegalStateException("Voucher " + codigo + " invalido, expirado ou ja utilizado.");
        }
        double co2Economizado = calcularCO2Economizado(modal, distanciaKm);
        this.status = StatusVoucher.UTILIZADO;
        return new Viagem(this, modal, distanciaKm, co2Economizado);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public OperadoraTransporte getOperadora() {
        return operadora;
    }

    public void setOperadora(OperadoraTransporte operadora) {
        this.operadora = operadora;
    }

    public double getValorCredito() {
        return valorCredito;
    }

    public void setValorCredito(double valorCredito) {
        this.valorCredito = valorCredito;
    }

    public int getPontosUtilizados() {
        return pontosUtilizados;
    }

    public void setPontosUtilizados(int pontosUtilizados) {
        this.pontosUtilizados = pontosUtilizados;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public StatusVoucher getStatus() {
        return status;
    }

    public void setStatus(StatusVoucher status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Voucher{codigo='" + codigo + "', valorCredito=" + valorCredito + ", status=" + status + "}";
    }
}
