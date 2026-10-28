package com.pointway.dto;

/** Pedido de conversao de pontos em voucher de transporte. */
public record EmitirVoucherRequest(Integer usuarioId, Integer operadoraId, Integer pontos) {
}
