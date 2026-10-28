package com.pointway.dao;

import com.pointway.model.OperadoraTransporte;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OperadoraDAO {

    public int inserir(OperadoraTransporte operadora) throws SQLException {
        String sql = "INSERT INTO operadoras (nome, cidade, sistema_bilhetagem) VALUES (?, ?, ?)";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, operadora.getNome());
            stmt.setString(2, operadora.getCidade());
            stmt.setString(3, operadora.getSistemaBilhetagem());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    operadora.setId(chaves.getInt(1));
                }
            }
        }
        return operadora.getId();
    }

    public OperadoraTransporte buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM operadoras WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? montarOperadora(rs) : null;
            }
        }
    }

    public List<OperadoraTransporte> listarTodos() throws SQLException {
        return listar("SELECT * FROM operadoras ORDER BY id", null);
    }

    public List<OperadoraTransporte> listarPorCidade(String cidade) throws SQLException {
        return listar("SELECT * FROM operadoras WHERE LOWER(cidade) = LOWER(?) ORDER BY id", cidade);
    }

    public int contar() throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement("SELECT COUNT(*) FROM operadoras");
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean atualizar(OperadoraTransporte operadora) throws SQLException {
        String sql = "UPDATE operadoras SET nome = ?, cidade = ?, sistema_bilhetagem = ? WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, operadora.getNome());
            stmt.setString(2, operadora.getCidade());
            stmt.setString(3, operadora.getSistemaBilhetagem());
            stmt.setInt(4, operadora.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM operadoras WHERE id = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private List<OperadoraTransporte> listar(String sql, String parametro) throws SQLException {
        List<OperadoraTransporte> operadoras = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {
            if (parametro != null) {
                stmt.setString(1, parametro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    operadoras.add(montarOperadora(rs));
                }
            }
        }
        return operadoras;
    }

    private OperadoraTransporte montarOperadora(ResultSet rs) throws SQLException {
        return new OperadoraTransporte(rs.getInt("id"), rs.getString("nome"),
                rs.getString("cidade"), rs.getString("sistema_bilhetagem"));
    }
}
