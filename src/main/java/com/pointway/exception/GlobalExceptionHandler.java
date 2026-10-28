package com.pointway.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Converte as excecoes da aplicacao em respostas HTTP padronizadas,
 * para o front-end sempre receber um JSON com status e mensagem.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler({ValidacaoException.class, IllegalArgumentException.class})
    public ResponseEntity<ErroResposta> invalido(RuntimeException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, e.getMessage(), req);
    }

    @ExceptionHandler({RegraNegocioException.class, IllegalStateException.class})
    public ResponseEntity<ErroResposta> regraNegocio(RuntimeException e, HttpServletRequest req) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), req);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> conflito(ConflitoException e, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "JSON invalido ou com campo em formato errado (ex.: modal deve ser ONIBUS, METRO, TREM ou BRT).", req);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErroResposta> parametroInvalido(Exception e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Parametro invalido na URL: " + e.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> rotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Endpoint nao encontrado.", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> metodoNaoPermitido(HttpRequestMethodNotSupportedException e,
            HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage(), req);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErroResposta> erroBanco(SQLException e, HttpServletRequest req) {
        LOG.error("Erro de banco de dados", e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao acessar o banco de dados. Tente novamente mais tarde.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> erroGenerico(Exception e, HttpServletRequest req) {
        LOG.error("Erro inesperado", e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.", req);
    }

    private ResponseEntity<ErroResposta> montar(HttpStatus status, String mensagem, HttpServletRequest req) {
        ErroResposta corpo = new ErroResposta(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensagem, req.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }
}
