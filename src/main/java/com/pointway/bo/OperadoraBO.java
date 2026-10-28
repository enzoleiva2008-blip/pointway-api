package com.pointway.bo;

import com.pointway.dao.OperadoraDAO;
import com.pointway.dao.VoucherDAO;
import com.pointway.dto.OperadoraRequest;
import com.pointway.exception.ConflitoException;
import com.pointway.exception.RecursoNaoEncontradoException;
import com.pointway.exception.ValidacaoException;
import com.pointway.model.OperadoraTransporte;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/** Regras de negocio e validacoes das operadoras de transporte parceiras. */
@Service
public class OperadoraBO {

    private final OperadoraDAO operadoraDAO;
    private final VoucherDAO voucherDAO;

    public OperadoraBO(OperadoraDAO operadoraDAO, VoucherDAO voucherDAO) {
        this.operadoraDAO = operadoraDAO;
        this.voucherDAO = voucherDAO;
    }

    public List<OperadoraTransporte> listar(String cidade) throws SQLException {
        if (cidade == null || cidade.isBlank()) {
            return operadoraDAO.listarTodos();
        }
        return operadoraDAO.listarPorCidade(cidade.trim());
    }

    public OperadoraTransporte buscarPorId(int id) throws SQLException {
        OperadoraTransporte operadora = operadoraDAO.buscarPorId(id);
        if (operadora == null) {
            throw new RecursoNaoEncontradoException("Operadora " + id + " nao encontrada.");
        }
        return operadora;
    }

    public OperadoraTransporte cadastrar(OperadoraRequest dados) throws SQLException {
        validar(dados);
        OperadoraTransporte operadora = new OperadoraTransporte(dados.nome().trim(), dados.cidade().trim(),
                textoOuNulo(dados.sistemaBilhetagem()));
        operadoraDAO.inserir(operadora);
        return operadora;
    }

    public OperadoraTransporte atualizar(int id, OperadoraRequest dados) throws SQLException {
        OperadoraTransporte operadora = buscarPorId(id);
        validar(dados);
        operadora.setNome(dados.nome().trim());
        operadora.setCidade(dados.cidade().trim());
        operadora.setSistemaBilhetagem(textoOuNulo(dados.sistemaBilhetagem()));
        operadoraDAO.atualizar(operadora);
        return operadora;
    }

    public void excluir(int id) throws SQLException {
        buscarPorId(id);
        if (voucherDAO.contarPorOperadora(id) > 0) {
            throw new ConflitoException("Operadora possui vouchers vinculados e nao pode ser excluida.");
        }
        operadoraDAO.deletar(id);
    }

    private void validar(OperadoraRequest dados) {
        if (dados == null) {
            throw new ValidacaoException("Corpo da requisicao vazio.");
        }
        if (dados.nome() == null || dados.nome().isBlank() || dados.nome().length() > 120) {
            throw new ValidacaoException("Nome da operadora e obrigatorio (ate 120 caracteres).");
        }
        if (dados.cidade() == null || dados.cidade().isBlank() || dados.cidade().length() > 80) {
            throw new ValidacaoException("Cidade e obrigatoria (ate 80 caracteres).");
        }
        if (dados.sistemaBilhetagem() != null && dados.sistemaBilhetagem().length() > 80) {
            throw new ValidacaoException("Sistema de bilhetagem deve ter ate 80 caracteres.");
        }
    }

    private String textoOuNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
