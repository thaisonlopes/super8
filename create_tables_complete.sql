-- =====================================================
-- SCRIPT COMPLETO PARA CRIAR TABELAS DO SUPER8
-- =====================================================

-- Limpar tabelas existentes (se houver)
DROP TABLE IF EXISTS pontuacoes_jogo CASCADE;
DROP TABLE IF EXISTS partidas CASCADE;
DROP TABLE IF EXISTS jogadores CASCADE;
DROP TABLE IF EXISTS jogos CASCADE;

-- =====================================================
-- TABELA: jogadores
-- =====================================================
CREATE TABLE jogadores (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABELA: jogos
-- =====================================================
CREATE TABLE jogos (
    id SERIAL PRIMARY KEY,
    codigo_jogo VARCHAR(20) NOT NULL UNIQUE,
    finalizado BOOLEAN DEFAULT FALSE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABELA: partidas
-- =====================================================
CREATE TABLE partidas (
    id SERIAL PRIMARY KEY,
    id_jogo INTEGER NOT NULL,
    rodada INTEGER NOT NULL,
    id_jogador1 INTEGER NOT NULL,
    id_jogador2 INTEGER NOT NULL,
    pontuacao_jogador1 INTEGER,
    pontuacao_jogador2 INTEGER,
    finalizada BOOLEAN DEFAULT FALSE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_jogo) REFERENCES jogos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_jogador1) REFERENCES jogadores(id) ON DELETE CASCADE,
    FOREIGN KEY (id_jogador2) REFERENCES jogadores(id) ON DELETE CASCADE
);

-- =====================================================
-- TABELA: pontuacoes_jogo
-- =====================================================
CREATE TABLE pontuacoes_jogo (
    id SERIAL PRIMARY KEY,
    id_jogo INTEGER NOT NULL,
    id_jogador INTEGER NOT NULL,
    pontuacao INTEGER DEFAULT 0,
    rodada INTEGER DEFAULT 0,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_jogo) REFERENCES jogos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_jogador) REFERENCES jogadores(id) ON DELETE CASCADE
);

-- =====================================================
-- ÍNDICES PARA MELHOR PERFORMANCE
-- =====================================================
CREATE INDEX idx_jogadores_nome ON jogadores(nome);
CREATE INDEX idx_jogos_codigo ON jogos(codigo_jogo);
CREATE INDEX idx_partidas_jogo ON partidas(id_jogo);
CREATE INDEX idx_partidas_rodada ON partidas(rodada);
CREATE INDEX idx_pontuacoes_jogo ON pontuacoes_jogo(id_jogo);
CREATE INDEX idx_pontuacoes_jogador ON pontuacoes_jogo(id_jogador);

-- =====================================================
-- TRIGGERS PARA ATUALIZAR data_atualizacao
-- =====================================================

-- Trigger para jogadores
CREATE OR REPLACE FUNCTION update_jogadores_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_jogadores_updated_at
    BEFORE UPDATE ON jogadores
    FOR EACH ROW
    EXECUTE FUNCTION update_jogadores_updated_at();

-- Trigger para jogos
CREATE OR REPLACE FUNCTION update_jogos_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_jogos_updated_at
    BEFORE UPDATE ON jogos
    FOR EACH ROW
    EXECUTE FUNCTION update_jogos_updated_at();

-- =====================================================
-- COMENTÁRIOS DAS TABELAS
-- =====================================================
COMMENT ON TABLE jogadores IS 'Tabela de jogadores cadastrados no sistema';
COMMENT ON TABLE jogos IS 'Tabela de jogos/torneios criados';
COMMENT ON TABLE partidas IS 'Tabela de partidas individuais dentro de cada rodada';
COMMENT ON TABLE pontuacoes_jogo IS 'Tabela de pontuações dos jogadores por jogo';

-- =====================================================
-- VERIFICAÇÃO FINAL
-- =====================================================
SELECT 'Tabelas criadas com sucesso!' as status;

-- Verificar se as tabelas foram criadas
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('jogadores', 'jogos', 'partidas', 'pontuacoes_jogo')
ORDER BY table_name; 