package com.pointway.bo;

import com.pointway.dao.UsuarioDAO;
import com.pointway.dao.ViagemDAO;
import com.pointway.dao.VoucherDAO;
import com.pointway.dto.AtualizarViagemRequest;
import com.pointway.dto.ImpactoResponse;
import com.pointway.dto.ViagemRequest;
import com.pointway.exception.RecursoNaoEncontradoException;
import com.pointway.exception.ValidacaoException;
import com.pointway.model.Viagem;
import com.pointway.model.Voucher;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/** Registro das viagens (uso do voucher na catraca) e calculo de impacto. */
@Service
public class ViagemBO {

    public static final double DISTANCIA_MAXIMA_KM = 200;

    private final ViagemDAO viagemDAO;
    private final VoucherDAO voucherDAO;
    private final UsuarioDAO usuarioDAO;
    private final VoucherBO voucherBO;
    private final UsuarioBO usuarioBO;

    public ViagemBO(ViagemDAO viagemDAO, VoucherDAO voucherDAO, UsuarioDAO usuarioDAO,
            VoucherBO voucherBO, UsuarioBO usuarioBO) {
        this.viagemDAO = viagemDAO;
        this.voucherDAO = voucherDAO;
        this.usuarioDAO = usuarioDAO;
        this.voucherBO = voucherBO;
        this.usuarioBO = usuarioBO;
    }

    public List<Viagem> listar(Integer usuarioId) throws SQLException {
        if (usuarioId == null) {
            return viagemDAO.listarTodas();
        }
        usuarioBO.buscarPorId(usuarioId);
        return viagemDAO.listarPorUsuario(usuarioId);
    }

    public Viagem buscarPorId(int id) throws SQLException {
        Viagem viagem = viagemDAO.buscarPorId(id);
        if (viagem == null) {
            throw new RecursoNaoEncontradoException("Viagem " + id + " nao encontrada.");
        }
        return viagem;
    }

    /** Valida o voucher na catraca: consome o voucher e grava a viagem com o CO2 economizado. */
    public Viagem registrar(ViagemRequest dados) throws SQLException {
        if (dados == null || dados.codigoVoucher() == null || dados.codigoVoucher().isBlank()) {
            throw new ValidacaoException("Informe o codigoVoucher.");
        }
        if (dados.modal() == null) {
            throw new ValidacaoException("Informe o modal: ONIBUS, METRO, TREM ou BRT.");
        }
        validarDistancia(dados.distanciaKm());

        Voucher voucher = voucherBO.buscarPorCodigo(dados.codigoVoucher());
        Viagem viagem = voucher.validarNaCatraca(dados.modal(), dados.distanciaKm());
        viagem.setCo2EconomizadoKg(UsuarioBO.arredondar(viagem.getCo2EconomizadoKg()));
        viagemDAO.inserir(viagem);
        voucherDAO.atualizarStatus(voucher.getId(), voucher.getStatus());
        return viagem;
    }

    /** Corrige modal/distancia e recalcula o CO2 economizado. */
    public Viagem atualizar(int id, AtualizarViagemRequest dados) throws SQLException {
        Viagem viagem = buscarPorId(id);
        if (dados == null || dados.modal() == null) {
            throw new ValidacaoException("Informe o modal: ONIBUS, METRO, TREM ou BRT.");
        }
        validarDistancia(dados.distanciaKm());
        double co2 = viagem.getVoucher().calcularCO2Economizado(dados.modal(), dados.distanciaKm());
        viagem.setModal(dados.modal());
        viagem.setDistanciaKm(dados.distanciaKm());
        viagem.setCo2EconomizadoKg(UsuarioBO.arredondar(co2));
        viagemDAO.atualizar(viagem);
        return viagem;
    }

    public void excluir(int id) throws SQLException {
        buscarPorId(id);
        viagemDAO.deletar(id);
    }

    /** Indicadores gerais da plataforma para a pagina inicial. */
    public ImpactoResponse impactoGeral() throws SQLException {
        List<Voucher> vouchers = voucherDAO.listarTodos();
        int pontos = vouchers.stream().mapToInt(Voucher::getPontosUtilizados).sum();
        double credito = vouchers.stream().mapToDouble(Voucher::getValorCredito).sum();
        return new ImpactoResponse(usuarioDAO.listarTodos().size(), vouchers.size(), pontos,
                UsuarioBO.arredondar(credito), viagemDAO.listarTodas().size(),
                UsuarioBO.arredondar(viagemDAO.somarCo2(null)));
    }

    private void validarDistancia(Double distanciaKm) {
        if (distanciaKm == null || distanciaKm <= 0 || distanciaKm > DISTANCIA_MAXIMA_KM) {
            throw new ValidacaoException("A distancia deve ser maior que 0 e no maximo "
                    + (int) DISTANCIA_MAXIMA_KM + " km.");
        }
    }
}
