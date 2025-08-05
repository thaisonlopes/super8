package com.beach.super8.data.repository

import android.util.Log
import com.beach.super8.data.entities.GameEntity
import com.beach.super8.data.entities.PlayerEntity
import com.beach.super8.data.entities.MatchEntity
import com.beach.super8.data.entities.PontuacaoJogoEntity
import com.beach.super8.model.Game
import com.beach.super8.model.Player
import com.beach.super8.model.Match
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class GameRepository(
    private val postgresRepository: HttpPostgresRepository
) {
    companion object {
        private const val TAG = "GameRepository"
    }

    // Estado local para cache
    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: Flow<Game?> = _currentGame.asStateFlow()

    private val _savedPlayers = MutableStateFlow<List<Player>>(emptyList())
    val savedPlayers: Flow<List<Player>> = _savedPlayers.asStateFlow()

    // Funções para Jogos
    suspend fun createGame(gameCode: String): Game {
        Log.d(TAG, "Criando novo jogo: $gameCode")
        
        val gameId = UUID.randomUUID().toString() // Manter String para compatibilidade com o modelo Game
        val game = Game(
            id = gameId,
            gameCode = gameCode,
            players = emptyList(),
            rounds = emptyList(),
            isFinished = false
        )
        
        // Salvar no PostgreSQL
        val gameEntity = GameEntity(
            id = 0, // Deixar o banco gerar automaticamente
            codigoJogo = game.gameCode,
            finalizado = game.isFinished
        )
        
        val postgresGameId = postgresRepository.insertGame(gameEntity)
        if (postgresGameId != null) {
            Log.d(TAG, "Jogo salvo no PostgreSQL com sucesso, ID: $postgresGameId")
            // Atualizar o jogo com o ID real do PostgreSQL
            val updatedGame = game.copy(id = postgresGameId.toString())
            _currentGame.value = updatedGame
            return updatedGame
        } else {
            Log.e(TAG, "Erro ao salvar jogo no PostgreSQL")
            _currentGame.value = game
            return game
        }
    }

    suspend fun loadGameByCode(gameCode: String): Game? {
        Log.d(TAG, "Carregando jogo por código: $gameCode")
        
        val gameEntity = postgresRepository.getGameByCode(gameCode)
        if (gameEntity != null) {
            // Carregar jogadores e partidas do jogo
            val players = loadPlayersForGame(gameEntity.id.toString())
            val matches = loadMatchesForGame(gameEntity.id.toString())
            
            val game = Game(
                id = gameEntity.id.toString(),
                gameCode = gameEntity.codigoJogo,
                players = players,
                rounds = emptyList(), // Por enquanto, vamos usar rounds vazios
                isFinished = gameEntity.finalizado
            )
            
            _currentGame.value = game
            return game
        }
        
        return null
    }

    suspend fun saveGame(game: Game): Boolean {
        Log.d(TAG, "Salvando jogo: ${game.gameCode}")
        
        val gameEntity = GameEntity(
            id = 0, // Deixar o banco gerar automaticamente
            codigoJogo = game.gameCode,
            finalizado = game.isFinished
        )
        
        val postgresGameId = postgresRepository.insertGame(gameEntity)
        if (postgresGameId != null) {
            Log.d(TAG, "Jogo atualizado no PostgreSQL com sucesso, ID: $postgresGameId")
            // Atualizar o jogo com o ID real do PostgreSQL
            val updatedGame = game.copy(id = postgresGameId.toString())
            _currentGame.value = updatedGame
            return true
        } else {
            Log.e(TAG, "Erro ao atualizar jogo no PostgreSQL")
            return false
        }
    }

    // Funções para Jogadores
    suspend fun addPlayerToGame(gameId: String, playerName: String): Player? {
        Log.d(TAG, "Adicionando jogador ao jogo: $playerName")
        
        // Verificar se jogador já existe no banco
        var playerEntity = postgresRepository.getPlayerByName(playerName)
        
        if (playerEntity == null) {
            // Criar novo jogador
            playerEntity = PlayerEntity(
                id = 0, // Deixar o banco gerar automaticamente
                nome = playerName
            )
            
            val success = postgresRepository.insertPlayer(playerEntity)
            if (!success) {
                Log.e(TAG, "Erro ao criar jogador no PostgreSQL")
                return null
            }
            
            // Buscar o jogador recém-criado para obter o ID gerado
            playerEntity = postgresRepository.getPlayerByName(playerName)
            if (playerEntity == null) {
                Log.e(TAG, "Erro ao buscar jogador recém-criado")
                return null
            }
        }
        
        val player = Player(
            id = playerEntity.id.toString(), // Converter Int para String
            name = playerEntity.nome,
            totalPoints = 0,
            gamesPlayed = 0
        )
        
        // Atualizar jogo atual
        val currentGame = _currentGame.value
        if (currentGame != null && currentGame.id == gameId) {
            val updatedPlayers = currentGame.players + player
            val updatedGame = currentGame.copy(players = updatedPlayers)
            _currentGame.value = updatedGame
        }
        
        return player
    }

    suspend fun updatePlayerScore(playerId: String, newScore: Int): Boolean {
        Log.d(TAG, "Atualizando pontuação do jogador: $playerId -> $newScore")
        
        val success = postgresRepository.updatePlayerScore(playerId, newScore)
        if (success) {
            // Atualizar estado local
            val currentGame = _currentGame.value
            if (currentGame != null) {
                val updatedPlayers = currentGame.players.map { player ->
                    if (player.id == playerId) {
                        player.copy(totalPoints = newScore)
                    } else {
                        player
                    }
                }
                val updatedGame = currentGame.copy(players = updatedPlayers)
                _currentGame.value = updatedGame
            }
        }
        
        return success
    }

    suspend fun loadAllPlayers(): List<Player> {
        Log.d(TAG, "=== GAMEREPOSITORY: CARREGANDO TODOS OS JOGADORES ===")
        
        try {
            Log.d(TAG, "Chamando postgresRepository.getAllPlayers()...")
            Log.d(TAG, "postgresRepository: $postgresRepository")
            Log.d(TAG, "postgresRepository class: ${postgresRepository::class.simpleName}")
            
            Log.d(TAG, "=== ANTES DE CHAMAR getAllPlayers ===")
            
            try {
                val playerEntities = postgresRepository.getAllPlayers()
                Log.d(TAG, "=== DEPOIS DE CHAMAR getAllPlayers ===")
                
                Log.d(TAG, "PlayerEntities retornados: ${playerEntities.size}")
                Log.d(TAG, "PlayerEntities: ${playerEntities.map { "${it.id} - ${it.nome}" }}")
                
                val players = playerEntities.map { entity ->
                    Player(
                        id = entity.id.toString(), // Converter Int para String para manter compatibilidade
                        name = entity.nome,
                        totalPoints = 0, // Pontuação será calculada por jogo
                        gamesPlayed = 0  // Será calculado baseado nos jogos
                    )
                }
                
                Log.d(TAG, "Players convertidos: ${players.size}")
                Log.d(TAG, "Players: ${players.map { "${it.id} - ${it.name}" }}")
                
                _savedPlayers.value = players
                Log.d(TAG, "✅ GAMEREPOSITORY: Jogadores carregados com sucesso: ${players.size}")
                return players
            } catch (e: Exception) {
                Log.e(TAG, "❌ GAMEREPOSITORY: Erro na chamada getAllPlayers", e)
                Log.e(TAG, "Mensagem: ${e.message}")
                Log.e(TAG, "Causa: ${e.cause}")
                Log.e(TAG, "Stack trace completo:", e)
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ GAMEREPOSITORY: Erro ao carregar jogadores", e)
            Log.e(TAG, "Mensagem: ${e.message}")
            Log.e(TAG, "Causa: ${e.cause}")
            Log.e(TAG, "Stack trace completo:", e)
            return emptyList()
        }
    }

    // Funções para Partidas
    suspend fun saveMatches(gameId: String, matches: List<Match>): Boolean {
        Log.d(TAG, "=== SALVANDO PARTIDAS ===")
        Log.d(TAG, "GameId recebido: '$gameId'")
        Log.d(TAG, "GameId convertido para Int: ${gameId.toIntOrNull()}")
        Log.d(TAG, "Salvando ${matches.size} partidas para o jogo: $gameId")
        
        var allSuccess = true
        
        for (match in matches) {
            val matchEntity = MatchEntity(
                idJogo = gameId.toIntOrNull() ?: 0, // Usar o ID real do jogo
                rodada = match.round,
                idJogador1 = match.player1.id.toIntOrNull() ?: 0,
                idJogador2 = match.player2.id.toIntOrNull() ?: 0,
                pontuacaoJogador1 = match.player1Score,
                pontuacaoJogador2 = match.player2Score,
                finalizada = match.isFinished
            )
            
            Log.d(TAG, "Criando MatchEntity com idJogo: ${matchEntity.idJogo}")
            Log.d(TAG, "Partida: ${match.player1.name} vs ${match.player2.name} - Rodada ${match.round}")
            
            val success = postgresRepository.insertMatch(matchEntity)
            if (!success) {
                Log.e(TAG, "Erro ao salvar partida: ${match.player1.name} vs ${match.player2.name}")
                allSuccess = false
            }
        }
        
        if (allSuccess) {
            // Atualizar estado local
            val currentGame = _currentGame.value
            if (currentGame != null && currentGame.id == gameId) {
                // Por enquanto, não vamos atualizar matches no Game
                // pois o modelo Game usa rounds, não matches
            }
        }
        
        return allSuccess
    }

    suspend fun updateMatchResult(matchId: Long, player1Score: Int, player2Score: Int): Boolean {
        Log.d(TAG, "Atualizando resultado da partida: $matchId -> $player1Score x $player2Score")
        
        // Por enquanto, vamos retornar true
        // Em uma implementação completa, atualizaríamos a partida no banco
        return true
    }

    suspend fun updateMatch(gameId: Int, round: Int, match: MatchEntity): Boolean {
        Log.d(TAG, "GameRepository: Atualizando partida $gameId - Rodada $round")
        return postgresRepository.updateMatch(gameId, round, match)
    }

    suspend fun getGeneralRanking(): List<Map<String, Any>> {
        Log.d(TAG, "GameRepository: Buscando ranking geral")
        return postgresRepository.getGeneralRanking()
    }

    suspend fun getFinishedGames(): List<Map<String, Any>> {
        Log.d(TAG, "GameRepository: Buscando jogos finalizados")
        return postgresRepository.getFinishedGames()
    }

    suspend fun getGameDetails(gameId: Int): Map<String, Any>? {
        Log.d(TAG, "GameRepository: Buscando detalhes do jogo $gameId")
        return postgresRepository.getGameDetails(gameId)
    }

    // Funções auxiliares
    private suspend fun loadPlayersForGame(gameId: String): List<Player> {
        // Por enquanto, vamos carregar todos os jogadores
        // Em uma implementação mais completa, teríamos uma tabela específica para jogadores por jogo
        return loadAllPlayers()
    }

    private suspend fun loadMatchesForGame(gameId: String): List<Match> {
        Log.d(TAG, "Carregando partidas do jogo: $gameId")
        
        val matchEntities = postgresRepository.getMatchesByGameId(gameId)
        val players = _currentGame.value?.players ?: emptyList()
        
        return matchEntities.mapNotNull { entity ->
            val player1 = players.find { it.id == entity.idJogador1.toString() }
            val player2 = players.find { it.id == entity.idJogador2.toString() }
            
            if (player1 != null && player2 != null) {
                Match(
                    id = entity.id.toInt(),
                    round = entity.rodada,
                    player1 = player1,
                    player2 = player2,
                    player1Score = entity.pontuacaoJogador1,
                    player2Score = entity.pontuacaoJogador2,
                    isFinished = entity.finalizada
                )
            } else {
                Log.w(TAG, "Jogadores não encontrados para partida: ${entity.idJogador1} vs ${entity.idJogador2}")
                null
            }
        }
    }

    // Função para finalizar jogo
    suspend fun finishGame(gameId: String): Boolean {
        Log.d(TAG, "Finalizando jogo: $gameId")
        
        val currentGame = _currentGame.value
        if (currentGame != null && currentGame.id == gameId) {
            val updatedGame = currentGame.copy(isFinished = true)
            return saveGame(updatedGame)
        }
        
        return false
    }

    // Função para limpar estado local
    fun clearCurrentGame() {
        _currentGame.value = null
    }
} 