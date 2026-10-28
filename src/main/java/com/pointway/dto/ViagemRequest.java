package com.pointway.dto;

import com.pointway.model.ModalTransporte;

/** Uso do voucher na catraca: gera uma viagem. */
public record ViagemRequest(String codigoVoucher, ModalTransporte modal, Double distanciaKm) {
}
