-- Script para criar tabelas do Super8 com PostgreSQL
-- Execute: psql -h 145.223.30.215 -U postgres -d bt -f create_tables_manual.sql

-- Remover tabelas existentes (se houver)
DROP TABLE IF EXISTS jogadores_jogo CASCADE;
DROP TABLE IF EXISTS partidas CASCADE;
DROP TABLE IF EXISTS jogadores CASCADE;
DROP TABLE IF EXISTS jogos CASCADE;

-- Tabela de jogos/torneios
CREATE TABLE IF NOT EXISTS jogos (
    id VARCHAR(50) PRIMARY KEY,
    codigo_jogo VARCHAR(20) UNIQUE NOT NULL,
    finalizado BOOLEAN DEFAULT FALSE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de jogadores (dados básicos)
CREATE TABLE IF NOT EXISTS jogadores (
    id VARCHAR(50) PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de pontuações por jogo (movimentação)
CREATE TABLE IF NOT EXISTS pontuacoes_jogo (
    id SERIAL PRIMARY KEY,
    id_jogo VARCHAR(50) NOT NULL,
    id_jogador VARCHAR(50) NOT NULL,
    pontuacao INTEGER DEFAULT 0,
    rodada INTEGER DEFAULT 0,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_jogo) REFERENCES jogos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_jogador) REFERENCES jogadores(id) ON DELETE CASCADE,
    UNIQUE(id_jogo, id_jogador, rodada)
);

-- Tabela de partidas sorteadas
CREATE TABLE IF NOT EXISTS partidas (
    id SERIAL PRIMARY KEY,
    id_jogo VARCHAR(50) NOT NULL,
    rodada INTEGER NOT NULL,
    id_jogador1 VARCHAR(50) NOT NULL,
    id_jogador2 VARCHAR(50) NOT NULL,
    pontuacao_jogador1 INTEGER DEFAULT NULL,
    pontuacao_jogador2 INTEGER DEFAULT NULL,
    finalizada BOOLEAN DEFAULT FALSE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_jogo) REFERENCES jogos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_jogador1) REFERENCES jogadores(id) ON DELETE CASCADE,
    FOREIGN KEY (id_jogador2) REFERENCES jogadores(id) ON DELETE CASCADE
);

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_jogos_codigo ON jogos(codigo_jogo);
CREATE INDEX IF NOT EXISTS idx_jogadores_nome ON jogadores(nome);
CREATE INDEX IF NOT EXISTS idx_pontuacoes_jogo ON pontuacoes_jogo(id_jogo, id_jogador);
CREATE INDEX IF NOT EXISTS idx_partidas_jogo ON partidas(id_jogo, rodada);

-- Inserir alguns dados de teste (opcional)
-- INSERT INTO jogadores (id, nome) VALUES 
-- ('1', 'João'),
-- ('2', 'Maria'),
-- ('3', 'Pedro'),
-- ('4', 'Ana');

SELECT 'Tabelas criadas com sucesso!' as resultado; 