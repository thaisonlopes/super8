package com.beach.super8.data.repository

import android.util.Log
import com.beach.super8.data.entities.GameEntity
import com.beach.super8.data.entities.PlayerEntity
import com.beach.super8.data.entities.MatchEntity
import com.beach.super8.data.entities.PontuacaoJogoEntity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class UltraSimplePostgresRepository {
    companion object {
        private const val TAG = "UltraSimplePostgresRepository"
        private const val DB_URL = "jdbc:postgresql://145.223.30.215:5432/bt"
        private const val DB_USER = "postgres"
        private const val DB_PASSWORD = "alt@2024"
    }

    private fun getConnection(): Connection? {
        return try {
            Log.d(TAG, "Tentando conectar ao PostgreSQL...")
            Class.forName("com.impossibl.postgres.jdbc.PGDriver")
            
            // Usar Properties para configuração mais estável
            val props = java.util.Properties()
            props.setProperty("user", DB_USER)
            props.setProperty("password", DB_PASSWORD)
            props.setProperty("ssl", "false")
            props.setProperty("sslmode", "disable")
            props.setProperty("ApplicationName", "Super8Android")
            
            val simpleUrl = "jdbc:pgsql://145.223.30.215:5432/bt"
            Log.d(TAG, "URL de conexão: $simpleUrl")
            
            val connection = DriverManager.getConnection(simpleUrl, props)
            Log.d(TAG, "✅ Conexão estabelecida com sucesso!")
            return connection
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao conectar ao PostgreSQL: ${e.message}", e)
            Log.e(TAG, "Stack trace completo:", e)
            null
        }
    }

    suspend fun initializeDatabase(): Boolean {
        return try {
            val connection = getConnection()
            if (connection != null) {
                Log.d(TAG, "✅ Conexão com PostgreSQL estabelecida com sucesso!")
                connection.close()
                true
            } else {
                Log.e(TAG, "❌ Falha ao conectar ao PostgreSQL")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar banco: ${e.message}", e)
            false
        }
    }

    // === OPERAÇÕES DE JOGOS ===
    suspend fun insertGame(game: GameEntity): Boolean {
        return try {
            val connection = getConnection() ?: return false
            val sql = "INSERT INTO jogos (codigo_jogo, finalizado, data_criacao, data_atualizacao) VALUES (?, ?, ?, ?)"
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, game.codigoJogo)
                stmt.setBoolean(2, game.finalizado)
                stmt.setTimestamp(3, java.sql.Timestamp(game.dataCriacao.time))
                stmt.setTimestamp(4, java.sql.Timestamp(game.dataAtualizacao.time))
                
                val result = stmt.executeUpdate()
                connection.close()
                Log.d(TAG, "Jogo inserido: ${game.codigoJogo}")
                result > 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inserir jogo: ${e.message}", e)
            false
        }
    }

    suspend fun getGameByCode(gameCode: String): GameEntity? {
        return try {
            val connection = getConnection() ?: return null
            val sql = "SELECT id, codigo_jogo, finalizado, data_criacao, data_atualizacao FROM jogos WHERE codigo_jogo = ?"
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, gameCode)
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    val game = GameEntity(
                        id = rs.getInt("id"),
                        codigoJogo = rs.getString("codigo_jogo"),
                        finalizado = rs.getBoolean("finalizado"),
                        dataCriacao = rs.getTimestamp("data_criacao"),
                        dataAtualizacao = rs.getTimestamp("data_atualizacao")
                    )
                    connection.close()
                    Log.d(TAG, "Jogo encontrado: ${game.codigoJogo}")
                    game
                } else {
                    connection.close()
                    Log.d(TAG, "Jogo não encontrado: $gameCode")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar jogo: ${e.message}", e)
            null
        }
    }

    // === OPERAÇÕES DE JOGADORES ===
    suspend fun insertPlayer(player: PlayerEntity): Boolean {
        return try {
            Log.d(TAG, "=== INICIANDO INSERT PLAYER ===")
            Log.d(TAG, "Player ID: ${player.id}")
            Log.d(TAG, "Player Nome: ${player.nome}")
            
            val connection = getConnection()
            if (connection == null) {
                Log.e(TAG, "❌ Falha ao obter conexão")
                return false
            }
            Log.d(TAG, "✅ Conexão obtida com sucesso")
            
            val sql = "INSERT INTO jogadores (nome, data_criacao, data_atualizacao) VALUES (?, ?, ?)"
            Log.d(TAG, "SQL preparado: $sql")
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, player.nome)
                stmt.setTimestamp(2, java.sql.Timestamp(player.dataCriacao.time))
                stmt.setTimestamp(3, java.sql.Timestamp(player.dataAtualizacao.time))
                
                Log.d(TAG, "Parâmetros definidos, executando query...")
                val result = stmt.executeUpdate()
                Log.d(TAG, "Resultado do executeUpdate: $result")
                
                connection.close()
                Log.d(TAG, "✅ Conexão fechada")
                
                if (result > 0) {
                    Log.d(TAG, "✅ Jogador inserido com sucesso: ${player.nome}")
                } else {
                    Log.e(TAG, "❌ Nenhuma linha afetada na inserção")
                }
                
                result > 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inserir jogador: ${e.message}", e)
            Log.e(TAG, "Stack trace completo:", e)
            false
        }
    }

    suspend fun getPlayerByName(playerName: String): PlayerEntity? {
        return try {
            val connection = getConnection() ?: return null
            val sql = "SELECT id, nome, data_criacao, data_atualizacao FROM jogadores WHERE nome = ?"
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, playerName)
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    val player = PlayerEntity(
                        id = rs.getInt("id"),
                        nome = rs.getString("nome"),
                        dataCriacao = rs.getTimestamp("data_criacao"),
                        dataAtualizacao = rs.getTimestamp("data_atualizacao")
                    )
                    connection.close()
                    Log.d(TAG, "Jogador encontrado: ${player.nome}")
                    player
                } else {
                    connection.close()
                    Log.d(TAG, "Jogador não encontrado: $playerName")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar jogador: ${e.message}", e)
            null
        }
    }

    suspend fun getAllPlayers(): List<PlayerEntity> {
        return try {
            val connection = getConnection() ?: return emptyList()
            val sql = "SELECT id, nome, data_criacao, data_atualizacao FROM jogadores ORDER BY nome"
            
            connection.prepareStatement(sql).use { stmt ->
                val rs = stmt.executeQuery()
                val players = mutableListOf<PlayerEntity>()
                
                while (rs.next()) {
                    players.add(PlayerEntity(
                        id = rs.getInt("id"),
                        nome = rs.getString("nome"),
                        dataCriacao = rs.getTimestamp("data_criacao"),
                        dataAtualizacao = rs.getTimestamp("data_atualizacao")
                    ))
                }
                
                connection.close()
                Log.d(TAG, "Jogadores carregados: ${players.size}")
                players
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar jogadores: ${e.message}", e)
            emptyList()
        }
    }

    // === OPERAÇÕES DE PONTUAÇÕES ===
    suspend fun insertPontuacao(pontuacao: PontuacaoJogoEntity): Boolean {
        return try {
            val connection = getConnection() ?: return false
            val sql = "INSERT INTO pontuacoes_jogo (id_jogo, id_jogador, pontuacao, rodada, data_criacao) VALUES (?, ?, ?, ?, ?)"
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, pontuacao.idJogo)
                stmt.setInt(2, pontuacao.idJogador)
                stmt.setInt(3, pontuacao.pontuacao)
                stmt.setInt(4, pontuacao.rodada)
                stmt.setTimestamp(5, java.sql.Timestamp(pontuacao.dataCriacao.time))
                
                val result = stmt.executeUpdate()
                connection.close()
                Log.d(TAG, "Pontuação inserida: ${pontuacao.idJogador} - ${pontuacao.pontuacao} pontos")
                result > 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inserir pontuação: ${e.message}", e)
            false
        }
    }

    suspend fun getPontuacoesByJogo(gameId: String): List<PontuacaoJogoEntity> {
        return try {
            val connection = getConnection() ?: return emptyList()
            val sql = "SELECT id, id_jogo, id_jogador, pontuacao, rodada, data_criacao FROM pontuacoes_jogo WHERE id_jogo = ? ORDER BY rodada, id_jogador"
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, gameId)
                val rs = stmt.executeQuery()
                val pontuacoes = mutableListOf<PontuacaoJogoEntity>()
                
                while (rs.next()) {
                    pontuacoes.add(PontuacaoJogoEntity(
                        id = rs.getLong("id"),
                        idJogo = rs.getInt("id_jogo"),
                        idJogador = rs.getInt("id_jogador"),
                        pontuacao = rs.getInt("pontuacao"),
                        rodada = rs.getInt("rodada"),
                        dataCriacao = rs.getTimestamp("data_criacao")
                    ))
                }
                
                connection.close()
                Log.d(TAG, "Pontuações carregadas: ${pontuacoes.size}")
                pontuacoes
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar pontuações: ${e.message}", e)
            emptyList()
        }
    }

    // === OPERAÇÕES DE PARTIDAS ===
    suspend fun insertMatch(match: MatchEntity): Boolean {
        return try {
            val connection = getConnection() ?: return false
            val sql = "INSERT INTO partidas (id_jogo, rodada, id_jogador1, id_jogador2, pontuacao_jogador1, pontuacao_jogador2, finalizada, data_criacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, match.idJogo)
                stmt.setInt(2, match.rodada)
                stmt.setInt(3, match.idJogador1)
                stmt.setInt(4, match.idJogador2)
                stmt.setObject(5, match.pontuacaoJogador1)
                stmt.setObject(6, match.pontuacaoJogador2)
                stmt.setBoolean(7, match.finalizada)
                stmt.setTimestamp(8, java.sql.Timestamp(System.currentTimeMillis()))
                
                val result = stmt.executeUpdate()
                connection.close()
                Log.d(TAG, "Partida inserida: ${match.idJogador1} vs ${match.idJogador2}")
                result > 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inserir partida: ${e.message}", e)
            false
        }
    }

    suspend fun getMatchesByGameId(gameId: String): List<MatchEntity> {
        return try {
            val connection = getConnection() ?: return emptyList()
            val sql = "SELECT id, id_jogo, rodada, id_jogador1, id_jogador2, pontuacao_jogador1, pontuacao_jogador2, finalizada FROM partidas WHERE id_jogo = ? ORDER BY rodada"
            
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, gameId)
                val rs = stmt.executeQuery()
                val matches = mutableListOf<MatchEntity>()
                
                while (rs.next()) {
                    matches.add(MatchEntity(
                        id = rs.getLong("id"),
                        idJogo = rs.getInt("id_jogo"),
                        rodada = rs.getInt("rodada"),
                        idJogador1 = rs.getInt("id_jogador1"),
                        idJogador2 = rs.getInt("id_jogador2"),
                        pontuacaoJogador1 = rs.getObject("pontuacao_jogador1") as? Int,
                        pontuacaoJogador2 = rs.getObject("pontuacao_jogador2") as? Int,
                        finalizada = rs.getBoolean("finalizada")
                    ))
                }
                
                connection.close()
                Log.d(TAG, "Partidas carregadas: ${matches.size}")
                matches
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar partidas: ${e.message}", e)
            emptyList()
        }
    }

    // === FUNÇÕES AUXILIARES ===
    suspend fun updatePlayerScore(playerId: String, newScore: Int): Boolean {
        // Por enquanto, vamos apenas retornar true
        // Em uma implementação completa, atualizaríamos a pontuação no banco
        Log.d(TAG, "Atualizando pontuação do jogador $playerId para $newScore")
        return true
    }
} 