package com.pointway.config;

import com.pointway.dao.ConnectionFactory;
import com.pointway.dao.OperadoraDAO;
import com.pointway.model.OperadoraTransporte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ao iniciar a API, cria as tabelas (se ainda nao existirem) e cadastra
 * operadoras iniciais. Assim o deploy funciona sem rodar o script manualmente.
 * Pode ser desligado com a variavel de ambiente DB_INIT=false.
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final OperadoraDAO operadoraDAO;

    public DatabaseInitializer(OperadoraDAO operadoraDAO) {
        this.operadoraDAO = operadoraDAO;
    }

    @Override
    public void run(ApplicationArguments args) {
        if ("false".equalsIgnoreCase(System.getenv("DB_INIT"))) {
            return;
        }
        try {
            criarTabelas();
            cadastrarOperadorasIniciais();
            LOG.info("Banco de dados pronto.");
        } catch (SQLException | IOException e) {
            LOG.warn("Nao foi possivel preparar o banco de dados: {}", e.getMessage());
        }
    }

    private void criarTabelas() throws SQLException, IOException {
        String script = new ClassPathResource("schema.sql").getContentAsString(StandardCharsets.UTF_8);
        script = script.replaceAll("(?m)^--.*$", "");
        try (Connection conexao = ConnectionFactory.getConnection();
                Statement stmt = conexao.createStatement()) {
            for (String comando : script.split(";")) {
                if (!comando.isBlank()) {
                    stmt.execute(comando.trim());
                }
            }
        }
    }

    private void cadastrarOperadorasIniciais() throws SQLException {
        if (operadoraDAO.contar() > 0) {
            return;
        }
        operadoraDAO.inserir(new OperadoraTransporte("SPTrans", "Sao Paulo", "Bilhete Unico"));
        operadoraDAO.inserir(new OperadoraTransporte("Metro SP", "Sao Paulo", "Bilhete Unico"));
        operadoraDAO.inserir(new OperadoraTransporte("CPTM", "Sao Paulo", "Bilhete Unico"));
        operadoraDAO.inserir(new OperadoraTransporte("EMTU", "Regiao Metropolitana de SP", "Cartao BOM"));
    }
}
