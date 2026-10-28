package com.pointway.exception;

/** Lancada quando a operacao conflita com dados existentes, ex.: CPF repetido (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
