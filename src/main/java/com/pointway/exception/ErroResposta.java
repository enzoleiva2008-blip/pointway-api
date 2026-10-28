package com.pointway.exception;

import java.time.LocalDateTime;

/** Corpo JSON devolvido em qualquer erro da API. */
public record ErroResposta(LocalDateTime dataHora, int status, String erro, String mensagem, String caminho) {
}
