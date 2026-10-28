package com.pointway.dto;

/** Dados enviados para cadastrar ou atualizar uma operadora de transporte. */
public record OperadoraRequest(String nome, String cidade, String sistemaBilhetagem) {
}
