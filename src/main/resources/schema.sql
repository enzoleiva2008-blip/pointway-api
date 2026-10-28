-- Pointway - tabelas (mesmo modelo da Sprint 3)
-- Executado automaticamente na subida da API (DatabaseInitializer).

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL,
    saldo_pontos INT NOT NULL DEFAULT 0,
    data_cadastro DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS operadoras (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    cidade VARCHAR(80) NOT NULL,
    sistema_bilhetagem VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS vouchers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(36) NOT NULL UNIQUE,
    usuario_id INT NOT NULL,
    operadora_id INT NOT NULL,
    valor_credito DECIMAL(10,2) NOT NULL,
    pontos_utilizados INT NOT NULL,
    data_emissao DATE NOT NULL,
    data_validade DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_voucher_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_voucher_operadora FOREIGN KEY (operadora_id) REFERENCES operadoras(id)
);

CREATE TABLE IF NOT EXISTS viagens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    voucher_id INT NOT NULL,
    modal VARCHAR(20) NOT NULL,
    distancia_km DECIMAL(6,2) NOT NULL,
    co2_economizado_kg DECIMAL(6,2) NOT NULL,
    data_uso DATETIME NOT NULL,
    CONSTRAINT fk_viagem_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id)
);
