package com.beach.super8.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beach.super8.data.repository.GameRepository
import com.beach.super8.data.repository.HttpPostgresRepository
import com.beach.super8.model.Game
import com.beach.super8.model.Player
import com.beach.super8.model.Round
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.beach.super8.data.entities.MatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostgresGameViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "PostgresGameViewModel"
    }
    
    enum class GameCodeValidation {
        EMPTY, NOT_FOUND, FINISHED, VALID
    }
    
    // Repositories
    private val postgresRepository = HttpPostgresRepository()
    private val gameRepository = GameRepository(postgresRepository)
    
    // Proteção contra múltiplas chamadas simultâneas
    private var isCreatingGame = false
    private var isSavingRoundResults = false
    private var isLoadingPlayers = false
    
    // Estado dos jogadores salvos
    private val _savedPlayers = MutableStateFlow<List<Player>>(emptyList())
    val savedPlayers: StateFlow<List<Player>> = _savedPlayers.asStateFlow()
    
    // Estado do jogo atual
    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame.asStateFlow()
    
    // Estado dos jogos finalizados
    private val _finishedGames = MutableStateFlow<List<Game>>(emptyList())
    val finishedGames: StateFlow<List<Game>> = _finishedGames.asStateFlow()
    
    // Estado da rodada atual
    private val _currentRound = MutableStateFlow<Round?>(null)
    val currentRound: StateFlow<Round?> = _currentRound.asStateFlow()
    
    // Estado das estatísticas
    private val _statistics = MutableStateFlow(GameStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()
    
    init {
        Log.d(TAG, "=== INICIALIZANDO POSTGRESGAMEVIEWMODEL ===")
        initializeDatabase()
        
        // Carregar jogadores diretamente
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== CARREGANDO JOGADORES DIRETAMENTE ===")
                loadAllPlayers()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao carregar jogadores: ${e.message}", e)
            }
        }
        
        Log.d(TAG, "=== INICIALIZAÇÃO CONCLUÍDA ===")
    }
    
    // Inicializar banco de dados automaticamente
    private fun initializeDatabase() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Inicializando banco de dados PostgreSQL...")
                val success = postgresRepository.initializeDatabase()
                if (success) {
                    Log.d(TAG, "✅ Banco de dados inicializado com sucesso!")
                } else {
                    Log.e(TAG, "❌ Erro ao inicializar banco de dados")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inicializar banco: ${e.message}", e)
            }
        }
    }
    
    // Criar novo jogo
    fun createNewGame(gameCode: String, players: List<Player>) {
        if (isCreatingGame) {
            Log.d(TAG, "=== CREATE NEW GAME JÁ EM EXECUÇÃO, IGNORANDO ===")
            return
        }
        
        // Verificar se já existe um jogo com esse código
        if (_currentGame.value?.gameCode == gameCode) {
            Log.d(TAG, "=== JOGO JÁ EXISTE COM ESSE CÓDIGO, IGNORANDO ===")
            return
        }
        
        // Verificar se o gameCode não está vazio
        if (gameCode.isBlank()) {
            Log.d(TAG, "=== GAME CODE VAZIO, IGNORANDO ===")
            return
        }
        
        // Verificar se há jogadores
        if (players.isEmpty()) {
            Log.d(TAG, "=== LISTA DE JOGADORES VAZIA, IGNORANDO ===")
            return
        }
        
        isCreatingGame = true
        Log.d(TAG, "=== CREATE NEW GAME CHAMADO ===")
        Log.d(TAG, "GameCode: '$gameCode'")
        Log.d(TAG, "Players: ${players.map { it.name }}")
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Criando novo jogo com código: $gameCode e ${players.size} jogadores")
                
                // Criar cópias dos jogadores com pontos zerados para esta partida
                val gamePlayers = players.map { player ->
                    player.copy(totalPoints = 0)
                }
                
                val currentGame = gameRepository.createGame(gameCode)
                _currentGame.value = currentGame
                
                // Adicionar jogadores ao jogo
                gamePlayers.forEach { player ->
                    gameRepository.addPlayerToGame(currentGame.id, player.name)
                }
                
                // Gerar rodadas
                val updatedGame = currentGame.copy(players = gamePlayers)
                Log.d(TAG, "=== GERANDO RODADAS ===")
                Log.d(TAG, "UpdatedGame ID: '${updatedGame.id}'")
                Log.d(TAG, "UpdatedGame players: ${updatedGame.players.map { it.name }}")
                generateRounds(updatedGame)
                Log.d(TAG, "Rodadas geradas, jogo atualizado com ID: '${updatedGame.id}'")
                
                // Definir a primeira rodada como rodada atual
                val firstRound = _currentGame.value?.rounds?.firstOrNull()
                _currentRound.value = firstRound
                Log.d(TAG, "CurrentRound definido: ${_currentRound.value?.roundNumber}")
                
                Log.d(TAG, "Novo jogo criado com sucesso no PostgreSQL")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar novo jogo", e)
            } finally {
                isCreatingGame = false
            }
        }
    }
    
    // Carregar jogo por código
    fun loadGameByCode(gameCode: String): Boolean {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Carregando jogo por código: $gameCode")
                
                val game = gameRepository.loadGameByCode(gameCode)
                if (game != null) {
                    _currentGame.value = game
                    _currentRound.value = game.rounds.getOrNull(game.currentRound - 1)
                    Log.d(TAG, "Jogo carregado com sucesso")
                } else {
                    Log.d(TAG, "Jogo não encontrado")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar jogo", e)
            }
        }
        return false
    }
    
    // Adicionar jogador
    fun addPlayer(playerName: String) {
        viewModelScope.launch {
            try {
                val trimmedName = playerName.trim()
                if (trimmedName.isBlank()) {
                    Log.d(TAG, "Nome em branco, ignorando")
                    return@launch
                }
                
                Log.d(TAG, "=== INICIANDO ADIÇÃO DE JOGADOR ===")
                Log.d(TAG, "Nome do jogador: '$trimmedName'")
                
                // Verificar se já existe
                val existingPlayer = _savedPlayers.value.find { 
                    it.name.trim().equals(trimmedName, ignoreCase = true) 
                }
                
                if (existingPlayer != null) {
                    Log.d(TAG, "Jogador já existe: ${existingPlayer.name}")
                    return@launch
                }
                
                Log.d(TAG, "Jogador não existe, criando novo...")
                
                // Criar novo jogador no PostgreSQL
                val player = Player(name = trimmedName)
                Log.d(TAG, "Player criado com ID: ${player.id}")
                
                val playerEntity = com.beach.super8.data.entities.PlayerEntity(
                    id = 0, // Deixar o banco gerar automaticamente
                    nome = player.name
                )
                Log.d(TAG, "PlayerEntity criado: ${playerEntity.id} - ${playerEntity.nome}")
                
                Log.d(TAG, "Chamando postgresRepository.insertPlayer...")
                val success = postgresRepository.insertPlayer(playerEntity)
                Log.d(TAG, "Resultado do insertPlayer: $success")
                
                if (success) {
                    val currentList = _savedPlayers.value.toMutableList()
                    currentList.add(player)
                    _savedPlayers.value = currentList
                    Log.d(TAG, "✅ Jogador adicionado com sucesso!")
                    Log.d(TAG, "Total de jogadores agora: ${_savedPlayers.value.size}")
                } else {
                    Log.e(TAG, "❌ Erro ao adicionar jogador no PostgreSQL")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao adicionar jogador", e)
                Log.e(TAG, "Stack trace completo:", e)
            }
        }
    }
    
    // Atualizar pontuação de jogador (para desempate)
    fun updatePlayerScore(playerId: String, newScore: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Atualizando pontuação do jogador: $playerId -> $newScore")
                
                val success = gameRepository.updatePlayerScore(playerId, newScore)
                if (success) {
                    Log.d(TAG, "Pontuação atualizada com sucesso")
                } else {
                    Log.e(TAG, "Erro ao atualizar pontuação")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar pontuação", e)
            }
        }
    }
    
    // Atualizar placar da rodada
    fun updateRoundScore(roundIndex: Int, gameIndex: Int, pair1Score: Int, pair2Score: Int) {
        Log.d(TAG, "Atualizando placar da rodada $roundIndex, jogo $gameIndex: $pair1Score x $pair2Score")
        
        val game = _currentGame.value ?: return
        val updatedRounds = game.rounds.toMutableList()
        if (roundIndex < 0 || roundIndex >= updatedRounds.size) return
        
        val round = updatedRounds[roundIndex]
        fun isValidScore(s1: Int, s2: Int) = (s1 in 0..6 && s2 in 0..6 && s1 + s2 == 6)
        
        val updatedRound = when (gameIndex) {
            0 -> round.copy(
                game1 = round.game1.copy(
                    pair1Score = pair1Score,
                    pair2Score = pair2Score,
                    isCompleted = isValidScore(pair1Score, pair2Score)
                ),
                isCompleted = false
            )
            1 -> round.copy(
                game2 = round.game2.copy(
                    pair1Score = pair1Score,
                    pair2Score = pair2Score,
                    isCompleted = isValidScore(pair1Score, pair2Score)
                ),
                isCompleted = false
            )
            else -> round
        }
        
        val finalRound = if (updatedRound.game1.isCompleted && updatedRound.game2.isCompleted) {
            updatedRound.copy(isCompleted = true)
        } else {
            updatedRound.copy(isCompleted = false)
        }
        
        updatedRounds[roundIndex] = finalRound
        val updatedGame = game.copy(rounds = updatedRounds)
        recalculatePlayerPoints(updatedGame)
        _currentRound.value = finalRound
        _currentGame.value = updatedGame
        
        // NÃO salvar automaticamente - apenas atualizar a UI
        // O salvamento será feito apenas quando clicar em "Próxima Rodada"
        Log.d(TAG, "Placar atualizado na UI (não salvo no banco ainda)")
    }
    
    // Próxima rodada
    fun nextRound() {
        val game = _currentGame.value ?: return
        val currentRound = _currentRound.value ?: return
        val nextRoundNumber = game.currentRound + 1
        
        try {
            Log.d(TAG, "Passando para próxima rodada: $nextRoundNumber")
            
            // SALVAR APENAS SE A RODADA ATUAL ESTIVER COMPLETA
            if (currentRound.isCompleted) {
                Log.d(TAG, "Rodada ${currentRound.roundNumber} completa, salvando no banco...")
                saveRoundResults(currentRound, game.id)
                Log.d(TAG, "Rodada ${currentRound.roundNumber} salva no banco!")
                processRoundPoints(currentRound)
            }
            
            // IR PARA PRÓXIMA RODADA SE NÃO FOR A ÚLTIMA
            if (nextRoundNumber <= 7) {
                val nextRound = game.rounds.getOrNull(nextRoundNumber - 1)
                if (nextRound != null) {
                    _currentRound.value = nextRound
                    val updatedGame = game.copy(currentRound = nextRoundNumber)
                    _currentGame.value = updatedGame
                    Log.d(TAG, "Próxima rodada definida: $nextRoundNumber")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao passar para próxima rodada", e)
        }
    }
    
    // Finalizar torneio
    fun finishTournament() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Finalizando torneio")
                
                val game = _currentGame.value ?: return@launch
                val currentRound = _currentRound.value ?: return@launch
                
                // SALVAR A ÚLTIMA RODADA SE ESTIVER COMPLETA
                if (currentRound.isCompleted) {
                    Log.d(TAG, "Salvando última rodada (${currentRound.roundNumber}) antes de finalizar...")
                    saveRoundResults(currentRound, game.id)
                    Log.d(TAG, "Última rodada salva com sucesso!")
                    processRoundPoints(currentRound)
                }
                
                // RECALCULAR TODOS OS PONTOS DO JOGO
                Log.d(TAG, "Recalculando pontos finais do jogo...")
                recalculatePlayerPoints(game)
                
                // Finalizar o jogo
                val finishedGame = game.copy(isFinished = true)
                val success = gameRepository.finishGame(game.id)
                
                if (success) {
                    _currentGame.value = finishedGame
                    Log.d(TAG, "Torneio finalizado com sucesso")
                } else {
                    Log.e(TAG, "Erro ao finalizar torneio")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao finalizar torneio", e)
            }
        }
    }
    
    // Salvar jogo no histórico
    fun saveGameToHistory(game: Game) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Salvando jogo no histórico: ${game.gameCode}")
                
                val success = gameRepository.saveGame(game)
                if (success) {
                    Log.d(TAG, "Jogo salvo no histórico com sucesso")
                } else {
                    Log.e(TAG, "Erro ao salvar jogo no histórico")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar jogo no histórico", e)
            }
        }
    }
    
    // Carregar todos os jogadores
    private fun loadAllPlayers() {
        if (isLoadingPlayers) {
            Log.d(TAG, "=== LOAD ALL PLAYERS JÁ EM EXECUÇÃO, IGNORANDO ===")
            return
        }
        
        isLoadingPlayers = true
        Log.d(TAG, "=== CARREGANDO TODOS OS JOGADORES ===")
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Chamando gameRepository.getGeneralRanking()...")
                
                // Buscar ranking geral com pontos totais
                val rankingData = gameRepository.getGeneralRanking()
                Log.d(TAG, "Ranking geral retornado: ${rankingData.size} jogadores")
                
                // Converter para lista de Player com pontos totais
                val playersWithPoints = rankingData.map { playerData ->
                    val id = playerData["id"] as? Int ?: 0
                    val nome = playerData["nome"] as? String ?: ""
                    val jogosJogados = playerData["jogos_jogados"] as? Int ?: 0
                    val pontosTotais = playerData["pontos_totais"] as? Int ?: 0
                    
                    Player(
                        id = id.toString(),
                        name = nome,
                        totalPoints = pontosTotais,
                        gamesPlayed = jogosJogados
                    )
                }
                
                Log.d(TAG, "Jogadores com pontos totais: ${playersWithPoints.map { "${it.name}: ${it.totalPoints} pontos (${it.gamesPlayed} jogos)" }}")
                _savedPlayers.value = playersWithPoints
                Log.d(TAG, "✅ Ranking geral carregado: ${playersWithPoints.size} jogadores")
                Log.d(TAG, "=== CARREGAMENTO CONCLUÍDO ===")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao carregar ranking geral", e)
                Log.e(TAG, "Stack trace completo:", e)
                Log.e(TAG, "Mensagem de erro: ${e.message}")
                Log.e(TAG, "Causa: ${e.cause}")
                _savedPlayers.value = emptyList()
            } finally {
                isLoadingPlayers = false
            }
        }
    }
    
    // Recarregar ranking geral
    fun reloadGeneralRanking() {
        loadAllPlayers() // Esta função agora carrega o ranking geral com pontos totais
    }
    
    // Carregar jogos finalizados
    fun loadFinishedGames() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== CARREGANDO JOGOS FINALIZADOS ===")
                
                val gamesData = gameRepository.getFinishedGames()
                Log.d(TAG, "Jogos finalizados retornados: ${gamesData.size}")
                
                // Converter para lista de Game
                val finishedGames = gamesData.map { gameData ->
                    val id = gameData["id"] as? Int ?: 0
                    val codigoJogo = gameData["codigo_jogo"] as? String ?: ""
                    val finalizado = gameData["finalizado"] as? Boolean ?: false
                    val totalRodadas = gameData["total_rodadas"] as? Int ?: 0
                    val totalJogadores = gameData["total_jogadores"] as? Int ?: 0
                    
                    Game(
                        id = id.toString(),
                        gameCode = codigoJogo,
                        players = emptyList(), // Por enquanto vazio, pode ser carregado depois se necessário
                        rounds = emptyList(), // Por enquanto vazio, pode ser carregado depois se necessário
                        isFinished = finalizado
                    )
                }
                
                Log.d(TAG, "Jogos finalizados convertidos: ${finishedGames.map { "${it.gameCode} (${it.id})" }}")
                _finishedGames.value = finishedGames
                Log.d(TAG, "✅ Jogos finalizados carregados: ${finishedGames.size} jogos")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao carregar jogos finalizados", e)
                Log.e(TAG, "Stack trace completo:", e)
                Log.e(TAG, "Mensagem de erro: ${e.message}")
                Log.e(TAG, "Causa: ${e.cause}")
                _finishedGames.value = emptyList()
            }
        }
    }
    
    // Gerar código único
    fun generateUniqueGameCode(): String {
        var code: String
        do {
            code = generateRandomCode()
        } while (gameCodeExists(code))
        return code
    }
    
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
    
    fun gameCodeExists(code: String): Boolean {
        return _finishedGames.value.any { it.gameCode == code } ||
               _currentGame.value?.gameCode == code
    }
    
    // Funções auxiliares (mantidas do ViewModel original)
    private fun generateRounds(game: Game) {
        try {
            Log.d(TAG, "=== GERANDO RODADAS ===")
            Log.d(TAG, "Gerando rodadas para ${game.players.size} jogadores")
            Log.d(TAG, "Jogadores: ${game.players.map { it.name }}")
            
            val playersByPosition = game.players.withIndex().associate { (index, player) ->
                (index + 1) to player
            }
            
            Log.d(TAG, "PlayersByPosition: ${playersByPosition.map { "${it.key} -> ${it.value.name}" }}")
            
            val roundSchedule = listOf(
                listOf(
                    listOf(listOf(1, 5), listOf(7, 8)),
                    listOf(listOf(2, 3), listOf(4, 6))
                ),
                listOf(
                    listOf(listOf(4, 7), listOf(6, 8)),
                    listOf(listOf(1, 2), listOf(3, 5))
                ),
                listOf(
                    listOf(listOf(3, 4), listOf(5, 7)),
                    listOf(listOf(2, 6), listOf(1, 8))
                ),
                listOf(
                    listOf(listOf(1, 6), listOf(4, 5)),
                    listOf(listOf(3, 7), listOf(2, 8))
                ),
                listOf(
                    listOf(listOf(5, 6), listOf(2, 7)),
                    listOf(listOf(1, 4), listOf(3, 8))
                ),
                listOf(
                    listOf(listOf(4, 8), listOf(2, 5)),
                    listOf(listOf(6, 7), listOf(1, 3))
                ),
                listOf(
                    listOf(listOf(1, 7), listOf(2, 4)),
                    listOf(listOf(3, 6), listOf(5, 8))
                )
            )
            
            val rounds = mutableListOf<Round>()
            
            roundSchedule.forEachIndexed { roundIndex, games ->
                val roundNumber = roundIndex + 1
                val game1Positions = games[0]
                val game2Positions = games[1]
                
                Log.d(TAG, "Criando rodada $roundNumber:")
                Log.d(TAG, "  Jogo 1 posições: ${game1Positions[0]} vs ${game1Positions[1]}")
                Log.d(TAG, "  Jogo 2 posições: ${game2Positions[0]} vs ${game2Positions[1]}")
                
                val game1Player1 = playersByPosition[game1Positions[0][0]]
                val game1Player2 = playersByPosition[game1Positions[0][1]]
                val game1Player3 = playersByPosition[game1Positions[1][0]]
                val game1Player4 = playersByPosition[game1Positions[1][1]]
                
                val game2Player1 = playersByPosition[game2Positions[0][0]]
                val game2Player2 = playersByPosition[game2Positions[0][1]]
                val game2Player3 = playersByPosition[game2Positions[1][0]]
                val game2Player4 = playersByPosition[game2Positions[1][1]]
                
                Log.d(TAG, "  Jogo 1 jogadores: ${game1Player1?.name} + ${game1Player2?.name} vs ${game1Player3?.name} + ${game1Player4?.name}")
                Log.d(TAG, "  Jogo 2 jogadores: ${game2Player1?.name} + ${game2Player2?.name} vs ${game2Player3?.name} + ${game2Player4?.name}")
                
                if (game1Player1 != null && game1Player2 != null && game1Player3 != null && game1Player4 != null &&
                    game2Player1 != null && game2Player2 != null && game2Player3 != null && game2Player4 != null) {
                    
                    val game1 = com.beach.super8.model.GameMatch(
                        pair1 = Pair(game1Player1, game1Player2),
                        pair2 = Pair(game1Player3, game1Player4)
                    )
                    
                    val game2 = com.beach.super8.model.GameMatch(
                        pair1 = Pair(game2Player1, game2Player2),
                        pair2 = Pair(game2Player3, game2Player4)
                    )
                    
                    rounds.add(Round(roundNumber, game1, game2))
                    Log.d(TAG, "  ✅ Rodada $roundNumber criada com sucesso")
                } else {
                    Log.e(TAG, "  ❌ Erro: jogadores nulos na rodada $roundNumber")
                }
            }
            
            val updatedGame = game.copy(rounds = rounds)
            _currentGame.value = updatedGame
            Log.d(TAG, "✅ Rodadas geradas: ${rounds.size}")
            Log.d(TAG, "=== GERAÇÃO DE RODADAS CONCLUÍDA ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao gerar rodadas", e)
            Log.e(TAG, "Stack trace completo:", e)
        }
    }
    
    private fun processRoundPoints(round: Round) {
        try {
            val game = _currentGame.value ?: return
            val updatedPlayers = game.players.toMutableList()
            
            Log.d(TAG, "=== PROCESSANDO PONTOS DA RODADA ${round.roundNumber} ===")
            Log.d(TAG, "Jogadores antes: ${updatedPlayers.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            if (round.game1.isCompleted) {
                Log.d(TAG, "Jogo 1 completo: ${round.game1.pair1Score} x ${round.game1.pair2Score}")
                val player1Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair1.first.id }
                val player2Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair1.second.id }
                val player3Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair2.first.id }
                val player4Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair2.second.id }
                
                if (player1Index >= 0) {
                    val oldPoints = updatedPlayers[player1Index].totalPoints
                    updatedPlayers[player1Index] = updatedPlayers[player1Index].copy(totalPoints = oldPoints + round.game1.pair1Score)
                    Log.d(TAG, "${round.game1.pair1.first.name}: $oldPoints + ${round.game1.pair1Score} = ${updatedPlayers[player1Index].totalPoints}")
                }
                if (player2Index >= 0) {
                    val oldPoints = updatedPlayers[player2Index].totalPoints
                    updatedPlayers[player2Index] = updatedPlayers[player2Index].copy(totalPoints = oldPoints + round.game1.pair1Score)
                    Log.d(TAG, "${round.game1.pair1.second.name}: $oldPoints + ${round.game1.pair1Score} = ${updatedPlayers[player2Index].totalPoints}")
                }
                if (player3Index >= 0) {
                    val oldPoints = updatedPlayers[player3Index].totalPoints
                    updatedPlayers[player3Index] = updatedPlayers[player3Index].copy(totalPoints = oldPoints + round.game1.pair2Score)
                    Log.d(TAG, "${round.game1.pair2.first.name}: $oldPoints + ${round.game1.pair2Score} = ${updatedPlayers[player3Index].totalPoints}")
                }
                if (player4Index >= 0) {
                    val oldPoints = updatedPlayers[player4Index].totalPoints
                    updatedPlayers[player4Index] = updatedPlayers[player4Index].copy(totalPoints = oldPoints + round.game1.pair2Score)
                    Log.d(TAG, "${round.game1.pair2.second.name}: $oldPoints + ${round.game1.pair2Score} = ${updatedPlayers[player4Index].totalPoints}")
                }
            }
            
            if (round.game2.isCompleted) {
                Log.d(TAG, "Jogo 2 completo: ${round.game2.pair1Score} x ${round.game2.pair2Score}")
                val player1Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair1.first.id }
                val player2Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair1.second.id }
                val player3Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair2.first.id }
                val player4Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair2.second.id }
                
                if (player1Index >= 0) {
                    val oldPoints = updatedPlayers[player1Index].totalPoints
                    updatedPlayers[player1Index] = updatedPlayers[player1Index].copy(totalPoints = oldPoints + round.game2.pair1Score)
                    Log.d(TAG, "${round.game2.pair1.first.name}: $oldPoints + ${round.game2.pair1Score} = ${updatedPlayers[player1Index].totalPoints}")
                }
                if (player2Index >= 0) {
                    val oldPoints = updatedPlayers[player2Index].totalPoints
                    updatedPlayers[player2Index] = updatedPlayers[player2Index].copy(totalPoints = oldPoints + round.game2.pair1Score)
                    Log.d(TAG, "${round.game2.pair1.second.name}: $oldPoints + ${round.game2.pair1Score} = ${updatedPlayers[player2Index].totalPoints}")
                }
                if (player3Index >= 0) {
                    val oldPoints = updatedPlayers[player3Index].totalPoints
                    updatedPlayers[player3Index] = updatedPlayers[player3Index].copy(totalPoints = oldPoints + round.game2.pair2Score)
                    Log.d(TAG, "${round.game2.pair2.first.name}: $oldPoints + ${round.game2.pair2Score} = ${updatedPlayers[player3Index].totalPoints}")
                }
                if (player4Index >= 0) {
                    val oldPoints = updatedPlayers[player4Index].totalPoints
                    updatedPlayers[player4Index] = updatedPlayers[player4Index].copy(totalPoints = oldPoints + round.game2.pair2Score)
                    Log.d(TAG, "${round.game2.pair2.second.name}: $oldPoints + ${round.game2.pair2Score} = ${updatedPlayers[player4Index].totalPoints}")
                }
            }
            
            _currentGame.value = game.copy(players = updatedPlayers)
            Log.d(TAG, "Jogadores após processar pontos: ${updatedPlayers.map { "${it.name}: ${it.totalPoints} pontos" }}")
            Log.d(TAG, "=== PONTOS DA RODADA ${round.roundNumber} PROCESSADOS ===")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar pontos da rodada", e)
        }
    }
    
    fun recalculatePlayerPoints(game: Game) {
        try {
            // ZERAR pontos de todos os jogadores para calcular apenas este jogo
            val updatedPlayers = game.players.map { it.copy(totalPoints = 0) }.toMutableList()
            
            game.rounds.forEach { round ->
                if (round.game1.isCompleted) {
                    val p1 = round.game1.pair1.first
                    val p2 = round.game1.pair1.second
                    val p3 = round.game1.pair2.first
                    val p4 = round.game1.pair2.second
                    val s1 = round.game1.pair1Score
                    val s2 = round.game1.pair2Score
                    
                    updatedPlayers.find { it.id == p1.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s1)
                    }
                    updatedPlayers.find { it.id == p2.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s1)
                    }
                    updatedPlayers.find { it.id == p3.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s2)
                    }
                    updatedPlayers.find { it.id == p4.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s2)
                    }
                }
                
                if (round.game2.isCompleted) {
                    val p1 = round.game2.pair1.first
                    val p2 = round.game2.pair1.second
                    val p3 = round.game2.pair2.first
                    val p4 = round.game2.pair2.second
                    val s1 = round.game2.pair1Score
                    val s2 = round.game2.pair2Score
                    
                    updatedPlayers.find { it.id == p1.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s1)
                    }
                    updatedPlayers.find { it.id == p2.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s1)
                    }
                    updatedPlayers.find { it.id == p3.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s2)
                    }
                    updatedPlayers.find { it.id == p4.id }?.let { player ->
                        val idx = updatedPlayers.indexOf(player)
                        updatedPlayers[idx] = player.copy(totalPoints = player.totalPoints + s2)
                    }
                }
            }
            
            val updatedGame = game.copy(players = updatedPlayers)
            _currentGame.value = updatedGame
            Log.d(TAG, "Pontos recalculados para jogo atual: ${updatedPlayers.map { "${it.name}: ${it.totalPoints} pontos" }}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recalcular pontos", e)
        }
    }
    
    private fun saveGame(game: Game) {
        viewModelScope.launch {
            try {
                val success = gameRepository.saveGame(game)
                if (success) {
                    Log.d(TAG, "Jogo salvo com sucesso")
                } else {
                    Log.e(TAG, "Erro ao salvar jogo")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar jogo", e)
            }
        }
    }
    
    // Limpar jogo atual
    fun clearCurrentGame() {
        gameRepository.clearCurrentGame()
        _currentGame.value = null
        _currentRound.value = null
    }
    
    // Validar código do jogo
    fun validateGameCode(gameCode: String): GameCodeValidation {
        return when {
            gameCode.isBlank() -> GameCodeValidation.EMPTY
            else -> {
                // Por enquanto, vamos retornar VALID
                // Em uma implementação completa, verificaríamos no banco
                GameCodeValidation.VALID
            }
        }
    }
    
    // Funções que estavam no GameViewModel original
    fun loadHistoryIfNeeded() {
        // Por enquanto, não fazemos nada
        // Em uma implementação completa, carregaríamos do banco
    }
    
    fun previousRound() {
        val game = _currentGame.value ?: return
        val currentRound = _currentRound.value ?: return
        val previousRoundNumber = game.currentRound - 1
        
        if (previousRoundNumber >= 1) {
            val previousRound = game.rounds.getOrNull(previousRoundNumber - 1)
            if (previousRound != null) {
                _currentRound.value = previousRound
                val updatedGame = game.copy(currentRound = previousRoundNumber)
                _currentGame.value = updatedGame
            }
        }
    }
    
    fun saveCurrentGame() {
        val game = _currentGame.value ?: return
        saveGame(game)
    }
    
    fun updateGameRounds() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== ATUALIZANDO RODADAS NO BANCO ===")
                val game = _currentGame.value ?: return@launch
                
                // Salvar cada rodada que foi alterada
                game.rounds.forEach { round ->
                    if (round.game1.isCompleted || round.game2.isCompleted) {
                        Log.d(TAG, "Salvando rodada ${round.roundNumber} no banco...")
                        saveRoundResults(round, game.id)
                    }
                }
                
                // Recalcular pontos após as alterações
                recalculatePlayerPoints(game)
                
                Log.d(TAG, "=== RODADAS ATUALIZADAS NO BANCO ===")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar rodadas no banco", e)
            }
        }
    }
    
    fun updateAndSaveFinishedGames() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== ATUALIZANDO E SALVANDO JOGOS FINALIZADOS ===")
                val game = _currentGame.value ?: return@launch
                
                if (game.isFinished) {
                    // Salvar o jogo finalizado no histórico
                    val success = gameRepository.saveGame(game)
                    if (success) {
                        Log.d(TAG, "✅ Jogo finalizado salvo no histórico")
                        
                        // Atualizar a lista de jogos finalizados
                        val currentFinishedGames = _finishedGames.value.toMutableList()
                        val existingIndex = currentFinishedGames.indexOfFirst { it.id == game.id }
                        
                        if (existingIndex >= 0) {
                            currentFinishedGames[existingIndex] = game
                        } else {
                            currentFinishedGames.add(game)
                        }
                        
                        _finishedGames.value = currentFinishedGames
                        Log.d(TAG, "✅ Lista de jogos finalizados atualizada")
                    } else {
                        Log.e(TAG, "❌ Erro ao salvar jogo finalizado no histórico")
                    }
                }
                
                Log.d(TAG, "=== JOGOS FINALIZADOS ATUALIZADOS ===")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar jogos finalizados", e)
            }
        }
    }
    
    // Salvar resultados das partidas de uma rodada
    private fun saveRoundResults(round: Round, gameId: String) {
        if (isSavingRoundResults) {
            Log.d(TAG, "=== SAVE ROUND RESULTS JÁ EM EXECUÇÃO, IGNORANDO ===")
            return
        }
        
        isSavingRoundResults = true
        Log.d(TAG, "=== SALVANDO RESULTADOS DA RODADA ${round.roundNumber} ===")
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Salvando resultados da rodada ${round.roundNumber}")
                
                // Buscar IDs reais dos jogadores no banco
                val playerIds = mutableMapOf<String, Int>()
                val allPlayers = gameRepository.loadAllPlayers()
                
                // Mapear nomes para IDs
                allPlayers.forEach { player ->
                    playerIds[player.name] = player.id.toIntOrNull() ?: 0
                }
                
                Log.d(TAG, "IDs dos jogadores: $playerIds")
                
                // Salvar resultado do jogo 1
                if (round.game1.isCompleted) {
                    val idJogador1 = playerIds[round.game1.pair1.first.name] ?: 0
                    val idJogador2 = playerIds[round.game1.pair1.second.name] ?: 0
                    val idJogador3 = playerIds[round.game1.pair2.first.name] ?: 0
                    val idJogador4 = playerIds[round.game1.pair2.second.name] ?: 0
                    
                    Log.d(TAG, "Jogo 1 - IDs: $idJogador1, $idJogador2, $idJogador3, $idJogador4")
                    
                    val matchEntity1 = MatchEntity(
                        idJogo = gameId.toIntOrNull() ?: 0,
                        rodada = round.roundNumber,
                        idJogador1 = idJogador1,
                        idJogador2 = idJogador2,
                        pontuacaoJogador1 = round.game1.pair1Score,
                        pontuacaoJogador2 = round.game1.pair2Score,
                        finalizada = true
                    )
                    
                    // DURANTE O JOGO NORMAL: SEMPRE FAZER INSERT
                    val success1 = postgresRepository.insertMatch(matchEntity1)
                    if (success1) {
                        Log.d(TAG, "✅ Resultado do jogo 1 inserido: ${round.game1.pair1Score} x ${round.game1.pair2Score}")
                    } else {
                        Log.e(TAG, "❌ Erro ao salvar resultado do jogo 1")
                    }
                }
                
                // Salvar resultado do jogo 2
                if (round.game2.isCompleted) {
                    val idJogador1 = playerIds[round.game2.pair1.first.name] ?: 0
                    val idJogador2 = playerIds[round.game2.pair1.second.name] ?: 0
                    val idJogador3 = playerIds[round.game2.pair2.first.name] ?: 0
                    val idJogador4 = playerIds[round.game2.pair2.second.name] ?: 0
                    
                    Log.d(TAG, "Jogo 2 - IDs: $idJogador1, $idJogador2, $idJogador3, $idJogador4")
                    
                    val matchEntity2 = MatchEntity(
                        idJogo = gameId.toIntOrNull() ?: 0,
                        rodada = round.roundNumber,
                        idJogador1 = idJogador1,
                        idJogador2 = idJogador2,
                        pontuacaoJogador1 = round.game2.pair1Score,
                        pontuacaoJogador2 = round.game2.pair2Score,
                        finalizada = true
                    )
                    
                    // DURANTE O JOGO NORMAL: SEMPRE FAZER INSERT
                    val success2 = postgresRepository.insertMatch(matchEntity2)
                    if (success2) {
                        Log.d(TAG, "✅ Resultado do jogo 2 inserido: ${round.game2.pair1Score} x ${round.game2.pair2Score}")
                    } else {
                        Log.e(TAG, "❌ Erro ao salvar resultado do jogo 2")
                    }
                }
                
                Log.d(TAG, "=== RESULTADOS DA RODADA ${round.roundNumber} SALVOS ===")
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar resultados da rodada", e)
            } finally {
                isSavingRoundResults = false
            }
        }
    }
    
    // Editar partida no histórico (usa UPDATE)
    fun editMatchInHistory(gameId: String, roundNumber: Int, gameIndex: Int, pair1Score: Int, pair2Score: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== EDITANDO PARTIDA NO HISTÓRICO ===")
                Log.d(TAG, "Jogo: $gameId, Rodada: $roundNumber, Jogo: $gameIndex")
                
                // Buscar IDs reais dos jogadores no banco
                val playerIds = mutableMapOf<String, Int>()
                val allPlayers = gameRepository.loadAllPlayers()
                
                // Mapear nomes para IDs
                allPlayers.forEach { player ->
                    playerIds[player.name] = player.id.toIntOrNull() ?: 0
                }
                
                val game = _currentGame.value ?: return@launch
                val round = game.rounds.getOrNull(roundNumber - 1) ?: return@launch
                
                // Determinar qual jogo da rodada está sendo editado
                val matchEntity = when (gameIndex) {
                    0 -> {
                        val idJogador1 = playerIds[round.game1.pair1.first.name] ?: 0
                        val idJogador2 = playerIds[round.game1.pair1.second.name] ?: 0
                        MatchEntity(
                            idJogo = gameId.toIntOrNull() ?: 0,
                            rodada = roundNumber,
                            idJogador1 = idJogador1,
                            idJogador2 = idJogador2,
                            pontuacaoJogador1 = pair1Score,
                            pontuacaoJogador2 = pair2Score,
                            finalizada = true
                        )
                    }
                    1 -> {
                        val idJogador1 = playerIds[round.game2.pair1.first.name] ?: 0
                        val idJogador2 = playerIds[round.game2.pair1.second.name] ?: 0
                        MatchEntity(
                            idJogo = gameId.toIntOrNull() ?: 0,
                            rodada = roundNumber,
                            idJogador1 = idJogador1,
                            idJogador2 = idJogador2,
                            pontuacaoJogador1 = pair1Score,
                            pontuacaoJogador2 = pair2Score,
                            finalizada = true
                        )
                    }
                    else -> return@launch
                }
                
                // Fazer UPDATE da partida no histórico
                val success = postgresRepository.updateMatch(gameId.toIntOrNull() ?: 0, roundNumber, matchEntity)
                if (success) {
                    Log.d(TAG, "✅ Partida atualizada no histórico: $pair1Score x $pair2Score")
                } else {
                    Log.e(TAG, "❌ Erro ao atualizar partida no histórico")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao editar partida no histórico", e)
            }
        }
    }

    // Função pública para buscar detalhes do jogo (usada pela UI)
    suspend fun getGameDetailsForUI(gameId: Int): Map<String, Any>? {
        Log.d(TAG, "=== GETGAMEDETAILSFORUI CHAMADA PARA JOGO $gameId ===")
        try {
            val result = gameRepository.getGameDetails(gameId)
            Log.d(TAG, "Resultado do getGameDetails: $result")
            if (result != null) {
                val players = result["players"] as? List<Map<*, *>>
                val matches = result["matches"] as? List<Map<*, *>>
                Log.d(TAG, "Players encontrados: ${players?.size}")
                Log.d(TAG, "Matches encontrados: ${matches?.size}")
            }
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Erro em getGameDetailsForUI: ${e.message}", e)
            return null
        }
    }

    // Carregar detalhes de um jogo específico
    fun loadGameDetails(gameId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== CARREGANDO DETALHES DO JOGO $gameId ===")
                
                val gameDetails = gameRepository.getGameDetails(gameId)
                if (gameDetails != null) {
                    val game = gameDetails["game"] as? Map<*, *>
                    val players = gameDetails["players"] as? List<Map<*, *>>
                    val matches = gameDetails["matches"] as? List<Map<*, *>>
                    
                    if (game != null && players != null) {
                        // Converter para modelo Game
                        val gameId = game["id"] as? Int ?: 0
                        val codigoJogo = game["codigo_jogo"] as? String ?: ""
                        val finalizado = game["finalizado"] as? Boolean ?: false
                        
                        val gamePlayers = players.map { playerData ->
                            val id = playerData["id"] as? Int ?: 0
                            val nome = playerData["nome"] as? String ?: ""
                            val pontosTotais = when (val pontos = playerData["pontos_totais"]) {
                                is Number -> pontos.toInt()
                                is String -> pontos.toIntOrNull() ?: 0
                                else -> 0
                            }
                            
                            Player(
                                id = id.toString(),
                                name = nome,
                                totalPoints = pontosTotais,
                                gamesPlayed = 1
                            )
                        }
                        
                        // NÃO atualizar o _currentGame.value aqui para evitar sobrescrever os dados corretos
                        Log.d(TAG, "✅ Detalhes do jogo $gameId carregados: ${gamePlayers.size} jogadores")
                    }
                } else {
                    Log.e(TAG, "❌ Erro ao carregar detalhes do jogo $gameId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao carregar detalhes do jogo", e)
            }
        }
    }
} 