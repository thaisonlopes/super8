-- Script para verificar se as tabelas foram criadas
-- Execute este script no seu banco PostgreSQL

-- 1. Verificar se as tabelas existem
SELECT 
    table_name,
    CASE 
        WHEN table_name IS NOT NULL THEN 'EXISTE'
        ELSE 'NÃO EXISTE'
    END as status
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('jogadores', 'jogos', 'partidas', 'pontuacoes_jogo')
ORDER BY table_name;

-- 2. Verificar estrutura da tabela jogadores
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'jogadores' 
AND table_schema = 'public'
ORDER BY ordinal_position;

-- 3. Verificar dados na tabela jogadores
SELECT 
    id,
    nome,
    data_criacao,
    data_atualizacao
FROM jogadores 
ORDER BY data_criacao DESC 
LIMIT 10;

-- 4. Contar registros em cada tabela
SELECT 
    'jogadores' as tabela,
    COUNT(*) as total_registros
FROM jogadores
UNION ALL
SELECT 
    'jogos' as tabela,
    COUNT(*) as total_registros
FROM jogos
UNION ALL
SELECT 
    'partidas' as tabela,
    COUNT(*) as total_registros
FROM partidas
UNION ALL
SELECT 
    'pontuacoes_jogo' as tabela,
    COUNT(*) as total_registros
FROM pontuacoes_jogo; 