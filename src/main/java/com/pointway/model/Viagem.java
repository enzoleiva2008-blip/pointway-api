package com.pointway.model;

import java.time.LocalDateTime;

public class Viagem {

    private int id;
    private Voucher voucher;
    private ModalTransporte modal;
    private double distanciaKm;
    private double co2EconomizadoKg;
    private LocalDateTime dataUso;

    public Viagem() {
    }

    public Viagem(Voucher voucher, ModalTransporte modal, double distanciaKm, double co2EconomizadoKg) {
        this.voucher = voucher;
        this.modal = modal;
        this.distanciaKm = distanciaKm;
        this.co2EconomizadoKg = co2EconomizadoKg;
        this.dataUso = LocalDateTime.now();
    }

    public Viagem(int id, Voucher voucher, ModalTransporte modal, double distanciaKm, double co2EconomizadoKg,
            LocalDateTime dataUso) {
        this.id = id;
        this.voucher = voucher;
        this.modal = modal;
        this.distanciaKm = distanciaKm;
        this.co2EconomizadoKg = co2EconomizadoKg;
        this.dataUso = dataUso;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public ModalTransporte getModal() {
        return modal;
    }

    public void setModal(ModalTransporte modal) {
        this.modal = modal;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public double getCo2EconomizadoKg() {
        return co2EconomizadoKg;
    }

    public void setCo2EconomizadoKg(double co2EconomizadoKg) {
        this.co2EconomizadoKg = co2EconomizadoKg;
    }

    public LocalDateTime getDataUso() {
        return dataUso;
    }

    public void setDataUso(LocalDateTime dataUso) {
        this.dataUso = dataUso;
    }

    @Override
    public String toString() {
        return "Viagem{modal=" + modal + ", distanciaKm=" + distanciaKm
                + ", co2EconomizadoKg=" + co2EconomizadoKg + "}";
    }
}
