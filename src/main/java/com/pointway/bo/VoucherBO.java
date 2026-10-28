package com.pointway.bo;

import com.pointway.dao.UsuarioDAO;
import com.pointway.dao.ViagemDAO;
import com.pointway.dao.VoucherDAO;
import com.pointway.dto.AtualizarVoucherRequest;
import com.pointway.dto.EmitirVoucherRequest;
import com.pointway.exception.RecursoNaoEncontradoException;
import com.pointway.exception.RegraNegocioException;
import com.pointway.exception.ValidacaoException;
import com.pointway.model.OperadoraTransporte;
import com.pointway.model.StatusVoucher;
import com.pointway.model.Usuario;
import com.pointway.model.Voucher;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Conversao de pontos em vouchers e ciclo de vida do voucher. */
@Service
public class VoucherBO {

    public static final int VALIDADE_MAXIMA_DIAS = 90;

    private final VoucherDAO voucherDAO;
    private final UsuarioDAO usuarioDAO;
    private final ViagemDAO viagemDAO;
    private final UsuarioBO usuarioBO;
    private final OperadoraBO operadoraBO;

    public VoucherBO(VoucherDAO voucherDAO, UsuarioDAO usuarioDAO, ViagemDAO viagemDAO,
            UsuarioBO usuarioBO, OperadoraBO operadoraBO) {
        this.voucherDAO = voucherDAO;
        this.usuarioDAO = usuarioDAO;
        this.viagemDAO = viagemDAO;
        this.usuarioBO = usuarioBO;
        this.operadoraBO = operadoraBO;
    }

    /** Lista vouchers, com filtro opcional por usuario ou por status. */
    public List<Voucher> listar(Integer usuarioId, StatusVoucher status) throws SQLException {
        voucherDAO.expirarVencidos();
        if (usuarioId != null) {
            usuarioBO.buscarPorId(usuarioId);
            List<Voucher> vouchers = voucherDAO.listarPorUsuario(usuarioId);
            if (status != null) {
                vouchers.removeIf(v -> v.getStatus() != status);
            }
            return vouchers;
        }
        return status == null ? voucherDAO.listarTodos() : voucherDAO.listarPorStatus(status);
    }

    public Voucher buscarPorId(int id) throws SQLException {
        voucherDAO.expirarVencidos();
        Voucher voucher = voucherDAO.buscarPorId(id);
        if (voucher == null) {
            throw new RecursoNaoEncontradoException("Voucher " + id + " nao encontrado.");
        }
        return voucher;
    }

    public Voucher buscarPorCodigo(String codigo) throws SQLException {
        voucherDAO.expirarVencidos();
        Voucher voucher = codigo == null ? null : voucherDAO.buscarPorCodigo(codigo.trim());
        if (voucher == null) {
            throw new RecursoNaoEncontradoException("Voucher com codigo " + codigo + " nao encontrado.");
        }
        return voucher;
    }

    /**
     * Converte pontos em voucher: valida usuario e operadora, aplica a regra
     * antifraude (limite de resgates em 24h), desconta os pontos e grava o voucher.
     */
    public Voucher emitir(EmitirVoucherRequest dados) throws SQLException {
        if (dados == null || dados.usuarioId() == null || dados.operadoraId() == null || dados.pontos() == null) {
            throw new ValidacaoException("Informe usuarioId, operadoraId e pontos.");
        }
        Usuario usuario = usuarioBO.buscarPorId(dados.usuarioId());
        OperadoraTransporte operadora = operadoraBO.buscarPorId(dados.operadoraId());

        int resgates24h = voucherDAO.contarEmitidosDesde(usuario.getId(), LocalDate.now().minusDays(1));
        if (!usuario.podeResgatar(resgates24h)) {
            throw new RegraNegocioException("Limite de " + Usuario.LIMITE_RESGATES_24H
                    + " resgates em 24 horas atingido. Tente novamente amanha.");
        }

        Voucher voucher = Voucher.emitir(usuario, operadora, dados.pontos());
        usuarioDAO.atualizar(usuario);
        try {
            voucherDAO.inserir(voucher);
        } catch (SQLException e) {
            usuario.adicionarPontos(dados.pontos());
            usuarioDAO.atualizar(usuario);
            throw e;
        }
        return voucher;
    }

    /** Altera validade e/ou status. Voucher utilizado nao pode mais ser alterado. */
    public Voucher atualizar(int id, AtualizarVoucherRequest dados) throws SQLException {
        Voucher voucher = buscarPorId(id);
        if (dados == null || (dados.dataValidade() == null && dados.status() == null)) {
            throw new ValidacaoException("Informe dataValidade e/ou status.");
        }
        if (voucher.getStatus() == StatusVoucher.UTILIZADO) {
            throw new RegraNegocioException("Voucher ja utilizado nao pode ser alterado.");
        }
        if (dados.status() == StatusVoucher.UTILIZADO) {
            throw new RegraNegocioException("Para utilizar o voucher registre uma viagem (POST /viagens).");
        }
        if (dados.dataValidade() != null) {
            LocalDate limite = voucher.getDataEmissao().plusDays(VALIDADE_MAXIMA_DIAS);
            if (dados.dataValidade().isBefore(voucher.getDataEmissao()) || dados.dataValidade().isAfter(limite)) {
                throw new ValidacaoException("A validade deve ficar entre a emissao e " + limite + ".");
            }
            voucher.setDataValidade(dados.dataValidade());
        }
        if (dados.status() != null) {
            voucher.setStatus(dados.status());
        }
        if (voucher.getStatus() == StatusVoucher.ATIVO && voucher.getDataValidade().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Um voucher vencido nao pode ficar ATIVO; prorrogue a validade.");
        }
        voucherDAO.atualizar(voucher);
        return voucher;
    }

    /** Cancela um voucher ainda nao usado e devolve os pontos ao usuario. */
    public void cancelar(int id) throws SQLException {
        Voucher voucher = buscarPorId(id);
        if (voucher.getStatus() == StatusVoucher.UTILIZADO || viagemDAO.contarPorVoucher(id) > 0) {
            throw new RegraNegocioException("Voucher ja utilizado nao pode ser cancelado.");
        }
        Usuario usuario = voucher.getUsuario();
        usuario.adicionarPontos(voucher.getPontosUtilizados());
        usuarioDAO.atualizar(usuario);
        voucherDAO.deletar(id);
    }
}
