package com.pointway.dao;

import com.pointway.model.ModalTransporte;
import com.pointway.model.Viagem;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ViagemDAO {

    private final VoucherDAO voucherDAO;

    public ViagemDAO(VoucherDAO voucherDAO) {
        this.voucherDAO = voucherDAO;
    }

    public int inserir(Viagem viagem) throws SQLException {
        String sql = "INSERT INTO viagens (voucher_id, modal, distancia_km, co2_economizado_kg, data_uso) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, viagem.getVoucher().getId());
            stmt.setString(2, viagem.getModal().name());
            stmt.setDouble(3, viagem.getDistanciaKm());
            stmt.setDouble(4, viagem.getCo2EconomizadoKg());
            stmt.setTimestamp(5, Timestamp.valueOf(viagem.getDataUso()));
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    viagem.setId(chaves.getInt(1));
                }
            }
        }
        return viagem.getId();
    }

    public Viagem buscarPorId(int id) throws SQLException {
        List<Viagem> resultado = listar("SELECT * FROM viagens WHERE id = ?", id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Viagem> listarTodas() throws SQLException {
        return listar("SELECT * FROM viagens ORDER BY data_uso DESC");
    }

    public List<Viagem> listarPorUsuario(int usuarioId) throws SQLException {
        return listar("SELECT vi.* FROM viagens vi JOIN vouchers vo ON vo.id = vi.voucher_id "
                + "WHERE vo.usuario_id = ? ORDER BY vi.data_uso DESC", usuarioId);
    }

    public int contarPorVoucher(int voucherId) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(
                        "SELECT COUNT(*) FROM viagens WHERE voucher_id = ?")) {
            stmt.setInt(1, voucherId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Soma de CO2 economizado; com usuarioId nulo soma de todos os usuarios. */
    public double somarCo2(Integer usuarioId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(vi.co2_economizado_kg), 0) FROM viagens vi "
                + "JOIN vouchers vo ON vo.id = vi.voucher_id"
                + (usuarioId == null ? "" : " WHERE vo.usuario_id = ?");
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            if (usuarioId != null) {
                stmt.setInt(1, usuarioId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    public boolean atualizar(Viagem viagem) throws SQLException {
        String sql = "UPDATE viagens SET modal = ?, distancia_km = ?, co2_economizado_kg = ? WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, viagem.getModal().name());
            stmt.setDouble(2, viagem.getDistanciaKm());
            stmt.setDouble(3, viagem.getCo2EconomizadoKg());
            stmt.setInt(4, viagem.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM viagens WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private List<Viagem> listar(String sql, Object... parametros) throws SQLException {
        List<Viagem> viagens = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    viagens.add(montarViagem(rs));
                }
            }
        }
        return viagens;
    }

    private Viagem montarViagem(ResultSet rs) throws SQLException {
        return new Viagem(rs.getInt("id"), voucherDAO.buscarPorId(rs.getInt("voucher_id")),
                ModalTransporte.valueOf(rs.getString("modal")), rs.getDouble("distancia_km"),
                rs.getDouble("co2_economizado_kg"), rs.getTimestamp("data_uso").toLocalDateTime());
    }
}
