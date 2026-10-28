package com.pointway.exception;

/** Lancada quando uma regra de negocio impede a operacao (HTTP 422). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
