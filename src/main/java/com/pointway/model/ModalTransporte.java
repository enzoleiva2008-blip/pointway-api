package com.pointway.model;

public enum ModalTransporte {

    ONIBUS(0.09),
    METRO(0.04),
    TREM(0.05),
    BRT(0.07);

    private final double fatorReducaoCO2KgPorKm;

    ModalTransporte(double fatorReducaoCO2KgPorKm) {
        this.fatorReducaoCO2KgPorKm = fatorReducaoCO2KgPorKm;
    }

    public double getFatorReducaoCO2KgPorKm() {
        return fatorReducaoCO2KgPorKm;
    }
}
