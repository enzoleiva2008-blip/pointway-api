package com.pointway.dto;

/** Painel do usuario: saldo, quanto ja converteu e o impacto ambiental. */
public record ResumoUsuarioResponse(int usuarioId, String nome, int saldoPontos, double saldoEmReais,
        int totalVouchers, int vouchersAtivos, double creditoTotalGerado, int totalViagens,
        double co2EconomizadoKg) {
}
