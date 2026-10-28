package com.pointway.dao;

import com.pointway.model.StatusVoucher;
import com.pointway.model.Voucher;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VoucherDAO {

    private final UsuarioDAO usuarioDAO;
    private final OperadoraDAO operadoraDAO;

    public VoucherDAO(UsuarioDAO usuarioDAO, OperadoraDAO operadoraDAO) {
        this.usuarioDAO = usuarioDAO;
        this.operadoraDAO = operadoraDAO;
    }

    public int inserir(Voucher voucher) throws SQLException {
        String sql = "INSERT INTO vouchers (codigo, usuario_id, operadora_id, valor_credito, "
                + "pontos_utilizados, data_emissao, data_validade, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, voucher.getCodigo());
            stmt.setInt(2, voucher.getUsuario().getId());
            stmt.setInt(3, voucher.getOperadora().getId());
            stmt.setDouble(4, voucher.getValorCredito());
            stmt.setInt(5, voucher.getPontosUtilizados());
            stmt.setDate(6, Date.valueOf(voucher.getDataEmissao()));
            stmt.setDate(7, Date.valueOf(voucher.getDataValidade()));
            stmt.setString(8, voucher.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    voucher.setId(chaves.getInt(1));
                }
            }
        }
        return voucher.getId();
    }

    public Voucher buscarPorId(int id) throws SQLException {
        return buscarUm("SELECT * FROM vouchers WHERE id = ?", id);
    }

    public Voucher buscarPorCodigo(String codigo) throws SQLException {
        return buscarUm("SELECT * FROM vouchers WHERE codigo = ?", codigo);
    }

    public List<Voucher> listarTodos() throws SQLException {
        return listar("SELECT * FROM vouchers ORDER BY id DESC");
    }

    public List<Voucher> listarPorUsuario(int usuarioId) throws SQLException {
        return listar("SELECT * FROM vouchers WHERE usuario_id = ? ORDER BY id DESC", usuarioId);
    }

    public List<Voucher> listarPorStatus(StatusVoucher status) throws SQLException {
        return listar("SELECT * FROM vouchers WHERE status = ? ORDER BY id DESC", status.name());
    }

    /** Quantidade de vouchers emitidos pelo usuario a partir de uma data (regra antifraude). */
    public int contarEmitidosDesde(int usuarioId, LocalDate desde) throws SQLException {
        return contar("SELECT COUNT(*) FROM vouchers WHERE usuario_id = ? AND data_emissao >= ?",
                usuarioId, Date.valueOf(desde));
    }

    public int contarPorUsuario(int usuarioId) throws SQLException {
        return contar("SELECT COUNT(*) FROM vouchers WHERE usuario_id = ?", usuarioId);
    }

    public int contarPorOperadora(int operadoraId) throws SQLException {
        return contar("SELECT COUNT(*) FROM vouchers WHERE operadora_id = ?", operadoraId);
    }

    public boolean atualizar(Voucher voucher) throws SQLException {
        String sql = "UPDATE vouchers SET valor_credito = ?, pontos_utilizados = ?, "
                + "data_validade = ?, status = ? WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setDouble(1, voucher.getValorCredito());
            stmt.setInt(2, voucher.getPontosUtilizados());
            stmt.setDate(3, Date.valueOf(voucher.getDataValidade()));
            stmt.setString(4, voucher.getStatus().name());
            stmt.setInt(5, voucher.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarStatus(int id, StatusVoucher status) throws SQLException {
        String sql = "UPDATE vouchers SET status = ? WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Marca como EXPIRADO todo voucher ATIVO cuja validade ja passou. */
    public int expirarVencidos() throws SQLException {
        String sql = "UPDATE vouchers SET status = ? WHERE status = ? AND data_validade < ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, StatusVoucher.EXPIRADO.name());
            stmt.setString(2, StatusVoucher.ATIVO.name());
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            return stmt.executeUpdate();
        }
    }

    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM vouchers WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Voucher buscarUm(String sql, Object parametro) throws SQLException {
        List<Voucher> resultado = listar(sql, parametro);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    private List<Voucher> listar(String sql, Object... parametros) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            preencher(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vouchers.add(montarVoucher(rs));
                }
            }
        }
        return vouchers;
    }

    private int contar(String sql, Object... parametros) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            preencher(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void preencher(PreparedStatement stmt, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            stmt.setObject(i + 1, parametros[i]);
        }
    }

    private Voucher montarVoucher(ResultSet rs) throws SQLException {
        Voucher voucher = new Voucher();
        voucher.setId(rs.getInt("id"));
        voucher.setCodigo(rs.getString("codigo"));
        voucher.setUsuario(usuarioDAO.buscarPorId(rs.getInt("usuario_id")));
        voucher.setOperadora(operadoraDAO.buscarPorId(rs.getInt("operadora_id")));
        voucher.setValorCredito(rs.getDouble("valor_credito"));
        voucher.setPontosUtilizados(rs.getInt("pontos_utilizados"));
        voucher.setDataEmissao(rs.getDate("data_emissao").toLocalDate());
        voucher.setDataValidade(rs.getDate("data_validade").toLocalDate());
        voucher.setStatus(StatusVoucher.valueOf(rs.getString("status")));
        return voucher;
    }
}
