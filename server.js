const express = require('express');
const { Pool } = require('pg');
const cors = require('cors');

const app = express();
const port = 3000;

// Configuração do PostgreSQL
const pool = new Pool({
    host: '145.223.30.215',
    port: 5432,
    database: 'bt',
    user: 'postgres',
    password: 'alt@2024',
    max: 10, // Aumentado para 10 conexões
    idleTimeoutMillis: 120000, // 2 minutos
    connectionTimeoutMillis: 60000, // 1 minuto
    maxUses: 1000,
    allowExitOnIdle: true, // Permite sair quando ocioso
});

// Função para executar queries com retry
async function executeWithRetry(queryFn, maxRetries = 3) {
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            const client = await pool.connect();
            try {
                const result = await queryFn(client);
                return result;
            } finally {
                client.release();
            }
        } catch (err) {
            console.error(`❌ Tentativa ${attempt} falhou: ${err.message}`);
            if (attempt === maxRetries) {
                throw err;
            }
            // Aguardar antes de tentar novamente
            await new Promise(resolve => setTimeout(resolve, 1000 * attempt));
        }
    }
}

// Middleware
app.use(cors());
app.use(express.json());

// Teste de conexão
app.get('/api/test', async (req, res) => {
    try {
        const client = await pool.connect();
        const result = await client.query('SELECT NOW()');
        client.release();
        res.json({ success: true, message: 'Conexão OK', time: result.rows[0].now });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// Inserir jogador
app.post('/api/players', async (req, res) => {
    try {
        const { nome } = req.body;
        console.log(`📝 Inserindo jogador: ${nome}`);
        
        const client = await pool.connect();
        
        const result = await client.query(
            'INSERT INTO jogadores (nome, data_criacao, data_atualizacao) VALUES ($1, NOW(), NOW()) RETURNING *',
            [nome]
        );
        
        client.release();
        console.log(`✅ Jogador salvo no banco: ${nome} (ID: ${result.rows[0].id})`);
        res.json({ success: true, player: result.rows[0] });
    } catch (err) {
        console.error(`❌ Erro ao salvar jogador: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar todos os jogadores
app.get('/api/players', async (req, res) => {
    try {
        console.log('🔍 === INICIANDO BUSCA DE JOGADORES ===');
        console.log('📡 Conectando ao banco...');
        
        const result = await executeWithRetry(async (client) => {
            console.log('✅ Cliente conectado ao banco');
            console.log('📊 Executando query...');
            const result = await client.query('SELECT * FROM jogadores ORDER BY nome');
            console.log(`✅ Query executada, ${result.rows.length} jogadores encontrados`);
            console.log('🔓 Cliente liberado');
            return result;
        });
        
        console.log('📤 Enviando resposta...');
        res.json({ success: true, players: result.rows });
        console.log('✅ Resposta enviada com sucesso');
    } catch (err) {
        console.error('❌ === ERRO NA BUSCA DE JOGADORES ===');
        console.error('Erro:', err.message);
        console.error('Stack:', err.stack);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar jogador por nome
app.get('/api/players/:name', async (req, res) => {
    try {
        const { name } = req.params;
        const client = await pool.connect();
        const result = await client.query('SELECT * FROM jogadores WHERE nome = $1', [name]);
        client.release();
        
        if (result.rows.length > 0) {
            res.json({ success: true, player: result.rows[0] });
        } else {
            res.json({ success: true, player: null });
        }
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// Inserir jogo
app.post('/api/games', async (req, res) => {
    try {
        const { codigo_jogo, finalizado } = req.body;
        console.log(`📝 Inserindo jogo: ${codigo_jogo}`);
        
        const client = await pool.connect();
        
        const result = await client.query(
            'INSERT INTO jogos (codigo_jogo, finalizado, data_criacao, data_atualizacao) VALUES ($1, $2, NOW(), NOW()) RETURNING *',
            [codigo_jogo, finalizado]
        );
        
        client.release();
        console.log(`✅ Jogo salvo no banco: ${codigo_jogo} (ID: ${result.rows[0].id})`);
        res.json({ success: true, game: result.rows[0] });
    } catch (err) {
        console.error(`❌ Erro ao salvar jogo: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar jogo por código
app.get('/api/games/:code', async (req, res) => {
    try {
        const { code } = req.params;
        const client = await pool.connect();
        const result = await client.query('SELECT * FROM jogos WHERE codigo_jogo = $1', [code]);
        client.release();
        
        if (result.rows.length > 0) {
            res.json({ success: true, game: result.rows[0] });
        } else {
            res.json({ success: true, game: null });
        }
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// Inserir partida
app.post('/api/matches', async (req, res) => {
    try {
        const { id_jogo, rodada, id_jogador1, id_jogador2, pontuacao_jogador1, pontuacao_jogador2, finalizada } = req.body;
        console.log(`📝 Inserindo partida: Jogo ${id_jogo} - Rodada ${rodada}`);
        
        const result = await executeWithRetry(async (client) => {
            return await client.query(
                'INSERT INTO partidas (id_jogo, rodada, id_jogador1, id_jogador2, pontuacao_jogador1, pontuacao_jogador2, finalizada, data_criacao) VALUES ($1, $2, $3, $4, $5, $6, $7, NOW()) RETURNING *',
                [id_jogo, rodada, id_jogador1, id_jogador2, pontuacao_jogador1, pontuacao_jogador2, finalizada]
            );
        });
        
        console.log(`✅ Partida salva no banco: ${id_jogo} - Rodada ${rodada}`);
        res.json({ success: true, match: result.rows[0] });
    } catch (err) {
        console.error(`❌ Erro ao salvar partida: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Atualizar partida existente
app.put('/api/matches/:gameId/:round', async (req, res) => {
    try {
        const { gameId, round } = req.params;
        const { id_jogador1, id_jogador2, pontuacao_jogador1, pontuacao_jogador2, finalizada } = req.body;
        console.log(`📝 Atualizando partida: Jogo ${gameId} - Rodada ${round}`);
        
        const result = await executeWithRetry(async (client) => {
            return await client.query(
                'UPDATE partidas SET pontuacao_jogador1 = $1, pontuacao_jogador2 = $2, finalizada = $3 WHERE id_jogo = $4 AND rodada = $5 AND id_jogador1 = $6 AND id_jogador2 = $7 RETURNING *',
                [pontuacao_jogador1, pontuacao_jogador2, finalizada, gameId, round, id_jogador1, id_jogador2]
            );
        });
        
        if (result.rows.length > 0) {
            console.log(`✅ Partida atualizada no banco: ${gameId} - Rodada ${round}`);
            res.json({ success: true, match: result.rows[0] });
        } else {
            console.log(`❌ Partida não encontrada: ${gameId} - Rodada ${round}`);
            res.status(404).json({ success: false, error: 'Partida não encontrada' });
        }
    } catch (err) {
        console.error(`❌ Erro ao atualizar partida: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Inserir pontuação
app.post('/api/pontuacoes', async (req, res) => {
    try {
        const { id_jogo, id_jogador, pontuacao, rodada } = req.body;
        console.log(`📝 Inserindo pontuação: Jogo ${id_jogo} - Jogador ${id_jogador} - ${pontuacao} pontos`);
        
        const client = await pool.connect();
        
        const result = await client.query(
            'INSERT INTO pontuacoes_jogo (id_jogo, id_jogador, pontuacao, rodada, data_criacao) VALUES ($1, $2, $3, $4, NOW()) RETURNING *',
            [id_jogo, id_jogador, pontuacao, rodada]
        );
        
        client.release();
        console.log(`✅ Pontuação salva no banco: Jogador ${id_jogador} - ${pontuacao} pontos`);
        res.json({ success: true, pontuacao: result.rows[0] });
    } catch (err) {
        console.error(`❌ Erro ao salvar pontuação: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar partidas por jogo
app.get('/api/matches/:gameId', async (req, res) => {
    try {
        const { gameId } = req.params;
        const client = await pool.connect();
        const result = await client.query('SELECT * FROM partidas WHERE id_jogo = $1 ORDER BY rodada', [gameId]);
        client.release();
        res.json({ success: true, matches: result.rows });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar pontuações por jogo
app.get('/api/pontuacoes/:gameId', async (req, res) => {
    try {
        const { gameId } = req.params;
        const client = await pool.connect();
        const result = await client.query('SELECT * FROM pontuacoes_jogo WHERE id_jogo = $1 ORDER BY rodada, id_jogador', [gameId]);
        client.release();
        res.json({ success: true, pontuacoes: result.rows });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar ranking geral com pontos totais
app.get('/api/ranking-geral', async (req, res) => {
    try {
        console.log('🔍 === CALCULANDO RANKING GERAL ===');
        
        const result = await executeWithRetry(async (client) => {
            // Query para calcular pontos totais de cada jogador em todos os jogos
            const query = `
                SELECT 
                    j.id,
                    j.nome,
                    COUNT(DISTINCT p.id_jogo) as jogos_jogados,
                    COALESCE(SUM(
                        CASE 
                            WHEN j.id = p.id_jogador1 THEN p.pontuacao_jogador1
                            WHEN j.id = p.id_jogador2 THEN p.pontuacao_jogador2
                            ELSE 0
                        END
                    ), 0) as pontos_totais
                FROM jogadores j
                LEFT JOIN partidas p ON (j.id = p.id_jogador1 OR j.id = p.id_jogador2) AND p.finalizada = true
                GROUP BY j.id, j.nome
                ORDER BY pontos_totais DESC, j.nome
            `;
            
            return await client.query(query);
        });
        
        console.log(`✅ Ranking geral calculado: ${result.rows.length} jogadores`);
        res.json({ success: true, ranking: result.rows });
    } catch (err) {
        console.error(`❌ Erro ao calcular ranking geral: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar todos os jogos finalizados
app.get('/api/finished-games', async (req, res) => {
    try {
        console.log('🔍 === BUSCANDO JOGOS FINALIZADOS ===');
        
        const result = await executeWithRetry(async (client) => {
            const query = `
                SELECT 
                    j.id,
                    j.codigo_jogo,
                    j.finalizado,
                    j.data_criacao,
                    j.data_atualizacao,
                    COUNT(DISTINCT p.rodada) as total_rodadas,
                    COUNT(DISTINCT CASE WHEN p.id_jogador1 IS NOT NULL THEN p.id_jogador1 END) + 
                    COUNT(DISTINCT CASE WHEN p.id_jogador2 IS NOT NULL THEN p.id_jogador2 END) as total_jogadores
                FROM jogos j
                LEFT JOIN partidas p ON j.id = p.id_jogo AND p.finalizada = true
                WHERE EXISTS (SELECT 1 FROM partidas WHERE id_jogo = j.id AND finalizada = true)
                GROUP BY j.id, j.codigo_jogo, j.finalizado, j.data_criacao, j.data_atualizacao
                ORDER BY j.data_criacao DESC
            `;
            
            return await client.query(query);
        });
        
        console.log(`✅ Jogos finalizados encontrados: ${result.rows.length}`);
        res.json({ success: true, games: result.rows });
    } catch (err) {
        console.error(`❌ Erro ao buscar jogos finalizados: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

// Buscar dados completos de um jogo específico
app.get('/api/game-details/:gameId', async (req, res) => {
    try {
        const { gameId } = req.params;
        console.log(`🔍 === BUSCANDO DETALHES DO JOGO ${gameId} ===`);
        
        const result = await executeWithRetry(async (client) => {
            // Buscar dados do jogo
            const gameQuery = 'SELECT * FROM jogos WHERE id = $1';
            const gameResult = await client.query(gameQuery, [gameId]);
            
            if (gameResult.rows.length === 0) {
                return { game: null, matches: [], players: [] };
            }
            
            const game = gameResult.rows[0];
            
            // Buscar partidas do jogo
            const matchesQuery = `
                SELECT 
                    p.*,
                    j1.nome as jogador1_nome,
                    j2.nome as jogador2_nome
                FROM partidas p
                LEFT JOIN jogadores j1 ON p.id_jogador1 = j1.id
                LEFT JOIN jogadores j2 ON p.id_jogador2 = j2.id
                WHERE p.id_jogo = $1 AND p.finalizada = true
                ORDER BY p.rodada, p.id
            `;
            const matchesResult = await client.query(matchesQuery, [gameId]);
            
            // Buscar todos os jogadores que participaram
            const playersQuery = `
                SELECT DISTINCT 
                    j.id,
                    j.nome,
                    COALESCE(SUM(
                        CASE 
                            WHEN j.id = p.id_jogador1 THEN p.pontuacao_jogador1
                            WHEN j.id = p.id_jogador2 THEN p.pontuacao_jogador2
                            ELSE 0
                        END
                    ), 0) as pontos_totais
                FROM jogadores j
                INNER JOIN partidas p ON (j.id = p.id_jogador1 OR j.id = p.id_jogador2)
                WHERE p.id_jogo = $1 AND p.finalizada = true
                GROUP BY j.id, j.nome
                ORDER BY pontos_totais DESC
            `;
            const playersResult = await client.query(playersQuery, [gameId]);
            
            return {
                game: game,
                matches: matchesResult.rows,
                players: playersResult.rows
            };
        });
        
        console.log(`✅ Detalhes do jogo ${gameId} encontrados`);
        res.json({ success: true, data: result });
    } catch (err) {
        console.error(`❌ Erro ao buscar detalhes do jogo: ${err.message}`);
        res.status(500).json({ success: false, error: err.message });
    }
});

app.listen(port, '0.0.0.0', () => {
    console.log(`🚀 Servidor rodando em http://localhost:${port}`);
    console.log(`🌐 Servidor acessível em http://192.168.0.111:${port}`);
    console.log(`📊 API disponível em http://192.168.0.111:${port}/api`);
}); 