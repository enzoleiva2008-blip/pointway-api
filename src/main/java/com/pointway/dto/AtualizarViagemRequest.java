package com.pointway.dto;

import com.pointway.model.ModalTransporte;

/** Correcao de uma viagem registrada (o CO2 e recalculado). */
public record AtualizarViagemRequest(ModalTransporte modal, Double distanciaKm) {
}
