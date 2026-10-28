package com.pointway.dto;

/** Dados enviados pelo front-end para cadastrar ou atualizar um usuario. */
public record UsuarioRequest(String nome, String cpf, String email, Integer saldoPontos) {
}
