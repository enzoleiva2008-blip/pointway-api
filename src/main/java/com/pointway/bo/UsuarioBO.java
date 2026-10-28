package com.pointway.bo;

import com.pointway.dao.UsuarioDAO;
import com.pointway.dao.ViagemDAO;
import com.pointway.dao.VoucherDAO;
import com.pointway.dto.ResumoUsuarioResponse;
import com.pointway.dto.UsuarioRequest;
import com.pointway.exception.ConflitoException;
import com.pointway.exception.RecursoNaoEncontradoException;
import com.pointway.exception.RegraNegocioException;
import com.pointway.exception.ValidacaoException;
import com.pointway.model.Usuario;
import com.pointway.model.Voucher;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/** Regras de negocio e validacoes do usuario. */
@Service
public class UsuarioBO {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    public static final int LIMITE_PONTOS_POR_CREDITO = 100_000;

    private final UsuarioDAO usuarioDAO;
    private final VoucherDAO voucherDAO;
    private final ViagemDAO viagemDAO;

    public UsuarioBO(UsuarioDAO usuarioDAO, VoucherDAO voucherDAO, ViagemDAO viagemDAO) {
        this.usuarioDAO = usuarioDAO;
        this.voucherDAO = voucherDAO;
        this.viagemDAO = viagemDAO;
    }

    public List<Usuario> listar() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public Usuario buscarPorId(int id) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuario " + id + " nao encontrado.");
        }
        return usuario;
    }

    public Usuario buscarPorCpf(String cpf) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorCpf(formatarCpf(cpf));
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Nenhum usuario com o CPF informado.");
        }
        return usuario;
    }

    public Usuario cadastrar(UsuarioRequest dados) throws SQLException {
        validar(dados);
        String cpf = formatarCpf(dados.cpf());
        if (usuarioDAO.buscarPorCpf(cpf) != null) {
            throw new ConflitoException("Ja existe um usuario cadastrado com este CPF.");
        }
        int saldo = dados.saldoPontos() == null ? 0 : dados.saldoPontos();
        Usuario usuario = new Usuario(dados.nome().trim(), cpf, dados.email().trim().toLowerCase(), saldo);
        usuarioDAO.inserir(usuario);
        return usuario;
    }

    public Usuario atualizar(int id, UsuarioRequest dados) throws SQLException {
        Usuario usuario = buscarPorId(id);
        validar(dados);
        String cpf = formatarCpf(dados.cpf());
        Usuario donoDoCpf = usuarioDAO.buscarPorCpf(cpf);
        if (donoDoCpf != null && donoDoCpf.getId() != id) {
            throw new ConflitoException("Este CPF ja pertence a outro usuario.");
        }
        usuario.setNome(dados.nome().trim());
        usuario.setCpf(cpf);
        usuario.setEmail(dados.email().trim().toLowerCase());
        if (dados.saldoPontos() != null) {
            usuario.setSaldoPontos(dados.saldoPontos());
        }
        usuarioDAO.atualizar(usuario);
        return usuario;
    }

    /** Credita pontos ganhos na plataforma SoulUp ao usuario. */
    public Usuario adicionarPontos(int id, Integer pontos) throws SQLException {
        Usuario usuario = buscarPorId(id);
        if (pontos == null) {
            throw new ValidacaoException("Informe a quantidade de pontos.");
        }
        if (pontos > LIMITE_PONTOS_POR_CREDITO) {
            throw new RegraNegocioException("Limite de " + LIMITE_PONTOS_POR_CREDITO + " pontos por credito.");
        }
        usuario.adicionarPontos(pontos);
        usuarioDAO.atualizar(usuario);
        return usuario;
    }

    /** O usuario so pode ser excluido se nunca emitiu voucher (preserva o historico). */
    public void excluir(int id) throws SQLException {
        buscarPorId(id);
        if (voucherDAO.contarPorUsuario(id) > 0) {
            throw new ConflitoException("Usuario possui vouchers emitidos e nao pode ser excluido.");
        }
        usuarioDAO.deletar(id);
    }

    public ResumoUsuarioResponse resumo(int id) throws SQLException {
        Usuario usuario = buscarPorId(id);
        List<Voucher> vouchers = voucherDAO.listarPorUsuario(id);
        int ativos = (int) vouchers.stream().filter(Voucher::estaValido).count();
        double credito = vouchers.stream().mapToDouble(Voucher::getValorCredito).sum();
        int viagens = viagemDAO.listarPorUsuario(id).size();
        double co2 = arredondar(viagemDAO.somarCo2(id));
        return new ResumoUsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getSaldoPontos(),
                usuario.getSaldoPontos() / (double) Usuario.TAXA_PONTOS_POR_REAL, vouchers.size(), ativos,
                arredondar(credito), viagens, co2);
    }

    private void validar(UsuarioRequest dados) {
        if (dados == null) {
            throw new ValidacaoException("Corpo da requisicao vazio.");
        }
        if (dados.nome() == null || dados.nome().trim().length() < 3) {
            throw new ValidacaoException("Nome deve ter pelo menos 3 caracteres.");
        }
        if (dados.nome().trim().length() > 120) {
            throw new ValidacaoException("Nome deve ter no maximo 120 caracteres.");
        }
        if (dados.email() == null || !EMAIL.matcher(dados.email().trim()).matches()) {
            throw new ValidacaoException("E-mail invalido.");
        }
        if (dados.saldoPontos() != null && dados.saldoPontos() < 0) {
            throw new ValidacaoException("Saldo de pontos nao pode ser negativo.");
        }
        formatarCpf(dados.cpf());
    }

    /** Valida o CPF (digitos verificadores) e devolve no formato 000.000.000-00. */
    static String formatarCpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1 || !digitosConferem(digitos)) {
            throw new ValidacaoException("CPF invalido.");
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    private static boolean digitosConferem(String cpf) {
        for (int posicao = 9; posicao <= 10; posicao++) {
            int soma = 0;
            for (int i = 0; i < posicao; i++) {
                soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
            }
            int digito = (soma * 10) % 11 % 10;
            if (digito != cpf.charAt(posicao) - '0') {
                return false;
            }
        }
        return true;
    }

    static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
