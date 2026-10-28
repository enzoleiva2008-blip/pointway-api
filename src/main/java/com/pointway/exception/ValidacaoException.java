package com.pointway.exception;

/** Lancada quando os dados enviados sao invalidos (HTTP 400). */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
