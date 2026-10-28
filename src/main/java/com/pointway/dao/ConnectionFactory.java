package com.pointway.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexoes (padrao Connection Factory).
 * Os dados de conexao estao no codigo (valores padrao do MySQL local) e podem
 * ser trocados por variaveis de ambiente no deploy (DB_URL, DB_USER, DB_PASSWORD).
 */
public final class ConnectionFactory {

    private static final String URL_PADRAO =
            "jdbc:mysql://localhost:3306/pointway_db?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String USUARIO_PADRAO = "root";
    private static final String SENHA_PADRAO = "root";

    private static final String URL = lerVariavel("DB_URL", URL_PADRAO);
    private static final String USUARIO = lerVariavel("DB_USER", USUARIO_PADRAO);
    private static final String SENHA = lerVariavel("DB_PASSWORD", SENHA_PADRAO);

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    private static String lerVariavel(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}
