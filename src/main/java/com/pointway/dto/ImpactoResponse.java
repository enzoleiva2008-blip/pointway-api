package com.pointway.dto;

/** Numeros gerais da plataforma, usados na pagina inicial do front-end. */
public record ImpactoResponse(int totalUsuarios, int totalVouchers, int pontosConvertidos,
        double creditoGerado, int totalViagens, double co2EconomizadoKg) {
}
