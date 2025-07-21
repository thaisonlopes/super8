package com.beach.super8.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beach.super8.model.Game
import com.beach.super8.model.GameMatch
import com.beach.super8.model.Player
import com.beach.super8.model.Round
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel : ViewModel() {
    
    // Estado dos jogadores salvos
    private val _savedPlayers = MutableStateFlow<List<Player>>(emptyList())
    val savedPlayers: StateFlow<List<Player>> = _savedPlayers.asStateFlow()
    
    // Lista em memória para persistir jogadores entre sessões
    private val persistentPlayers = mutableListOf<Player>()
    
    // Contexto para SharedPreferences (será injetado)
    private var context: Context? = null
    
    // Estado do jogo atual
    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame.asStateFlow()
    
    // Estado dos jogos finalizados
    private val _finishedGames = MutableStateFlow<List<Game>>(emptyList())
    val finishedGames: StateFlow<List<Game>> = _finishedGames.asStateFlow()
    
    // Estado das estatísticas
    private val _statistics = MutableStateFlow(GameStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()
    
    // Estado da rodada atual
    private val _currentRound = MutableStateFlow<Round?>(null)
    val currentRound: StateFlow<Round?> = _currentRound.asStateFlow()
    
    // Função para definir o contexto
    fun setContext(context: Context) {
        this.context = context
        loadSavedPlayers()
    }
    
    // Função para carregar histórico quando necessário
    fun loadHistoryIfNeeded() {
        try {
            Log.d("GameViewModel", "=== LOAD HISTORY IF NEEDED ===")
            Log.d("GameViewModel", "Context: ${context != null}")
            Log.d("GameViewModel", "FinishedGames size: ${_finishedGames.value.size}")
            
            if (context != null) {
                Log.d("GameViewModel", "Carregando histórico...")
                loadFinishedGames()
                updateStatistics()
                Log.d("GameViewModel", "Histórico carregado! Size: ${_finishedGames.value.size}")
                
                // Log detalhado dos jogos carregados
                _finishedGames.value.forEach { game ->
                    Log.d("GameViewModel", "Jogo carregado: ${game.gameCode} - Jogadores: ${game.players.size} - Rodadas: ${game.rounds.size}")
                }
            } else {
                Log.d("GameViewModel", "Contexto não disponível para carregar histórico")
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO CARREGAR HISTÓRICO: ${e.message}", e)
        }
    }
    
    init {
        try {
            Log.d("GameViewModel", "Inicializando ViewModel")
            // Carregar dados básicos sem contexto
            Log.d("GameViewModel", "ViewModel inicializado com sucesso")
        } catch (e: Exception) {
            Log.e("GameViewModel", "Erro ao inicializar ViewModel", e)
        }
    }
    
    // Criar novo jogo
    fun createNewGame(gameCode: String, players: List<Player>) {
        try {
            Log.d("GameViewModel", "Criando novo jogo com código: $gameCode e ${players.size} jogadores")
            Log.d("GameViewModel", "Jogadores recebidos: ${players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            // Criar cópias dos jogadores com pontos zerados para esta partida
            val gamePlayers = players.map { player ->
                player.copy(totalPoints = 0) // Zerar pontos para esta partida
            }
            
            Log.d("GameViewModel", "Jogadores com pontos zerados: ${gamePlayers.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            val game = Game(
                gameCode = gameCode,
                players = gamePlayers,
                currentRound = 1
            )
            
            Log.d("GameViewModel", "Jogo criado, gerando rodadas")
            Log.d("GameViewModel", "Jogadores no jogo: ${game.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            _currentGame.value = game
            generateRounds(game)
            
            // Verificar se as rodadas foram geradas
            val updatedGame = _currentGame.value
            Log.d("GameViewModel", "Rodadas geradas: ${updatedGame?.rounds?.size}")
            Log.d("GameViewModel", "Primeira rodada: ${updatedGame?.rounds?.firstOrNull()}")
            Log.d("GameViewModel", "Jogadores após gerar rodadas: ${updatedGame?.players?.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            _currentRound.value = updatedGame?.rounds?.firstOrNull()
            Log.d("GameViewModel", "Rodada atual definida: ${_currentRound.value}")
            
            // SALVAR JOGO ATUAL
            saveCurrentGame()
            
            Log.d("GameViewModel", "Novo jogo criado com sucesso. Rodadas: ${game.rounds.size}")
        } catch (e: Exception) {
            Log.e("GameViewModel", "Erro ao criar novo jogo", e)
        }
    }
    
    // Entrar em jogo existente
    fun enterGame(gameCode: String): Boolean {
        val game = _finishedGames.value.find { it.gameCode == gameCode }
        if (game != null) {
            _currentGame.value = game
            _currentRound.value = game.rounds.getOrNull(game.currentRound - 1)
            return true
        }
        return false
    }
    
    // Salvar jogador
    fun savePlayer(player: Player) {
        try {
            Log.d("GameViewModel", "=== SALVANDO JOGADOR: ${player.name} ===")
            Log.d("GameViewModel", "Pontos do jogador: ${player.totalPoints}")
            
            val currentList = _savedPlayers.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { 
                it.name.trim().equals(player.name.trim(), ignoreCase = true) 
            }
            
            if (existingIndex >= 0) {
                // Jogador já existe - preservar pontos acumulados
                val existingPlayer = currentList[existingIndex]
                val updatedPlayer = existingPlayer.copy(
                    totalPoints = existingPlayer.totalPoints + player.totalPoints,
                    gamesPlayed = existingPlayer.gamesPlayed + player.gamesPlayed,
                    wins = existingPlayer.wins + player.wins,
                    losses = existingPlayer.losses + player.losses
                )
                
                Log.d("GameViewModel", "Jogador existente encontrado: ${existingPlayer.name}")
                Log.d("GameViewModel", "Pontos antigos: ${existingPlayer.totalPoints}, novos: ${player.totalPoints}, total: ${updatedPlayer.totalPoints}")
                
                currentList[existingIndex] = updatedPlayer
            } else {
                // Novo jogador
                Log.d("GameViewModel", "Novo jogador sendo adicionado: ${player.name}")
                currentList.add(player)
            }
            
            _savedPlayers.value = currentList
            savePlayersToPreferences(currentList)
            
            Log.d("GameViewModel", "Jogador salvo com sucesso: ${player.name} com ${player.totalPoints} pontos")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO SALVAR JOGADOR: ${e.message}", e)
        }
    }
    
    // Adicionar novo jogador
    fun addPlayer(playerName: String) {
        try {
            // Remover espaços em branco do início e fim
            val trimmedName = playerName.trim()
            
            // Verificar se o nome não está vazio após o trim
            if (trimmedName.isBlank()) {
                Log.d("GameViewModel", "Nome vazio após trim, ignorando")
                return
            }
            
            Log.d("GameViewModel", "=== ADICIONANDO NOVO JOGADOR: '$trimmedName' ===")
            Log.d("GameViewModel", "Nome original: '$playerName'")
            Log.d("GameViewModel", "Estado atual da lista: ${_savedPlayers.value.map { it.name }}")
            
            // Verificar se já existe (ignorando espaços)
            val existingPlayer = _savedPlayers.value.find { 
                it.name.trim().equals(trimmedName, ignoreCase = true) 
            }
            Log.d("GameViewModel", "Jogador existente encontrado: ${existingPlayer?.name}")
            
            if (existingPlayer != null) {
                Log.d("GameViewModel", "Jogador já existe: ${existingPlayer.name}")
                return
            }
            
            val newPlayer = Player(name = trimmedName)
            Log.d("GameViewModel", "Novo jogador criado: ${newPlayer.name} (ID: ${newPlayer.id})")
            
            val currentList = _savedPlayers.value.toMutableList()
            Log.d("GameViewModel", "Lista atual antes de adicionar: ${currentList.map { it.name }}")
            
            currentList.add(newPlayer)
            Log.d("GameViewModel", "Lista após adicionar: ${currentList.map { it.name }}")
            
            // Atualizar a lista de jogadores salvos
            _savedPlayers.value = currentList
            Log.d("GameViewModel", "StateFlow atualizado com ${currentList.size} jogadores")
            
            // Adicionar à lista persistente também
            persistentPlayers.add(newPlayer)
            Log.d("GameViewModel", "Adicionado à lista persistente. Total: ${persistentPlayers.size}")
            
            Log.d("GameViewModel", "Jogador adicionado com sucesso: $trimmedName")
            Log.d("GameViewModel", "Lista final: ${_savedPlayers.value.map { it.name }}")
            
            // Salvar nas preferências
            savePlayersToPreferences(currentList)
            Log.d("GameViewModel", "Jogador salvo com sucesso: $trimmedName")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO ADICIONAR JOGADOR: ${e.message}", e)
        }
    }
    
    // Adicionar jogador ao jogo atual
    fun addPlayerToCurrentGame(player: Player) {
        val game = _currentGame.value ?: return
        val updatedPlayers = game.players.toMutableList()
        
        if (updatedPlayers.size < 8 && !updatedPlayers.any { it.name == player.name }) {
            updatedPlayers.add(player)
            _currentGame.value = game.copy(players = updatedPlayers)
        }
    }
    
    // Próxima rodada
    fun nextRound() {
        val game = _currentGame.value ?: return
        val currentRound = _currentRound.value ?: return
        val nextRoundNumber = game.currentRound + 1
        
        try {
            Log.d("GameViewModel", "=== PASSANDO PARA PRÓXIMA RODADA ===")
            Log.d("GameViewModel", "Rodada atual: ${currentRound.roundNumber}")
            Log.d("GameViewModel", "Próxima rodada: $nextRoundNumber")
            
            // Verificar se a rodada atual está completa
            if (currentRound.isCompleted) {
                Log.d("GameViewModel", "Rodada atual está completa, processando pontos...")
                
                // Processar pontos da rodada atual
                processRoundPoints(currentRound)
                
                Log.d("GameViewModel", "Pontos processados com sucesso!")
                Log.d("GameViewModel", "Jogadores após processar pontos: ${_currentGame.value?.players?.map { "${it.name}: ${it.totalPoints} pontos" }}")
            } else {
                Log.d("GameViewModel", "Rodada atual não está completa, não processando pontos")
            }
            
            if (nextRoundNumber <= 7) { // 7 RODADAS COMPLETAS
                val nextRound = game.rounds.getOrNull(nextRoundNumber - 1)
                if (nextRound != null) {
                    Log.d("GameViewModel", "Navegando para rodada $nextRoundNumber")
                    
                    // Atualizar apenas a rodada atual, mantendo os jogadores com pontos
                    _currentRound.value = nextRound
                    
                    // Atualizar o jogo mantendo os jogadores com pontos processados
                    val updatedGame = _currentGame.value?.copy(currentRound = nextRoundNumber)
                    _currentGame.value = updatedGame
                    
                    // SALVAR JOGO ATUAL APÓS PASSAR RODADA
                    saveCurrentGame()
                    
                    Log.d("GameViewModel", "Jogadores após navegar: ${_currentGame.value?.players?.map { "${it.name}: ${it.totalPoints} pontos" }}")
                }
            }
            
            Log.d("GameViewModel", "=== NAVEGAÇÃO PARA PRÓXIMA RODADA CONCLUÍDA ===")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO PASSAR PARA PRÓXIMA RODADA: ${e.message}", e)
        }
    }
    
    fun previousRound() {
        _currentGame.value?.let { game ->
            if (game.currentRound > 1) {
                game.currentRound--
                _currentGame.value = game
                Log.d("GameViewModel", "Voltando para rodada ${game.currentRound}")
            }
        }
    }
    
    // Atualizar placar da rodada atual
    fun updateRoundScore(gameIndex: Int, pair1Score: Int, pair2Score: Int) {
        Log.d("GameViewModel", "=== FUNÇÃO UPDATE ROUND SCORE INICIADA ===")
        Log.d("GameViewModel", "Parâmetros recebidos: gameIndex=$gameIndex, pair1Score=$pair1Score, pair2Score=$pair2Score")
        
        try {
            Log.d("GameViewModel", "=== UPDATE ROUND SCORE CHAMADO ===")
            Log.d("GameViewModel", "GameIndex: $gameIndex, Pair1Score: $pair1Score, Pair2Score: $pair2Score")
            
            val round = _currentRound.value
            val game = _currentGame.value
            
            Log.d("GameViewModel", "Round atual: ${round != null}")
            Log.d("GameViewModel", "Game atual: ${game != null}")
            
            if (round == null) {
                Log.e("GameViewModel", "ERRO: Round atual é null!")
                return
            }
            
            if (game == null) {
                Log.e("GameViewModel", "ERRO: Game atual é null!")
                return
            }
            
            Log.d("GameViewModel", "Rodada atual: ${round.roundNumber}")
            Log.d("GameViewModel", "Jogadores antes da atualização: ${game.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            if (pair1Score + pair2Score != 6) {
                Log.d("GameViewModel", "Soma dos pontos não é 6 (${pair1Score + pair2Score}), ignorando")
                return
            }
            
            val updatedRound = when (gameIndex) {
                0 -> round.copy(
                    game1 = round.game1.copy(
                        pair1Score = pair1Score,
                        pair2Score = pair2Score,
                        isCompleted = true
                    )
                )
                1 -> round.copy(
                    game2 = round.game2.copy(
                        pair1Score = pair1Score,
                        pair2Score = pair2Score,
                        isCompleted = true
                    )
                )
                else -> {
                    Log.e("GameViewModel", "ERRO: GameIndex inválido: $gameIndex")
                    return
                }
            }
            
            // Verificar se ambos os jogos da rodada estão completos
            val isRoundCompleted = updatedRound.game1.isCompleted && updatedRound.game2.isCompleted
            
            val finalRound = if (isRoundCompleted) {
                updatedRound.copy(isCompleted = true)
            } else {
                updatedRound
            }
            
            Log.d("GameViewModel", "Rodada completada: $isRoundCompleted")
            
            // Atualizar rodada primeiro
            _currentRound.value = finalRound
            
            // Atualizar jogo com a nova rodada
            val updatedRounds = game.rounds.toMutableList()
            val roundIndex = updatedRounds.indexOfFirst { it.roundNumber == round.roundNumber }
            if (roundIndex >= 0) {
                updatedRounds[roundIndex] = finalRound
            }
            
            // Atualizar o jogo com as rodadas atualizadas
            _currentGame.value = game.copy(rounds = updatedRounds)
            
            // SALVAR JOGO ATUAL APÓS ATUALIZAR PLACAR
            saveCurrentGame()
            
            // NÃO processar pontos aqui - apenas quando a rodada for finalizada
            Log.d("GameViewModel", "Placar atualizado. Pontos serão processados quando a rodada for finalizada.")
            Log.d("GameViewModel", "Jogadores após atualização: ${_currentGame.value?.players?.map { "${it.name}: ${it.totalPoints} pontos" }}")
            Log.d("GameViewModel", "=== UPDATE ROUND SCORE CONCLUÍDO ===")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO ATUALIZAR PLACAR: ${e.message}", e)
        }
    }
    
    private fun updatePlayerPoints(gameMatch: com.beach.super8.model.GameMatch) {
        _currentGame.value?.let { currentGame ->
            // Atualizar pontos do time 1
            currentGame.players.find { it.name == gameMatch.pair1.first.name }?.let { player ->
                player.totalPoints += gameMatch.pair1Score
            }
            currentGame.players.find { it.name == gameMatch.pair1.second.name }?.let { player ->
                player.totalPoints += gameMatch.pair1Score
            }
            
            // Atualizar pontos do time 2
            currentGame.players.find { it.name == gameMatch.pair2.first.name }?.let { player ->
                player.totalPoints += gameMatch.pair2Score
            }
            currentGame.players.find { it.name == gameMatch.pair2.second.name }?.let { player ->
                player.totalPoints += gameMatch.pair2Score
            }
            
            Log.d("GameViewModel", "Pontos atualizados: ${currentGame.players.map { "${it.name}: ${it.totalPoints} pts" }}")
        }
    }
    
    // Finalizar torneio
    fun finishTournament() {
        val game = _currentGame.value ?: return
        val currentRound = _currentRound.value ?: return
        
        try {
            Log.d("GameViewModel", "=== FINALIZANDO TORNEIO ===")
            Log.d("GameViewModel", "Rodada atual: ${currentRound.roundNumber}")
            Log.d("GameViewModel", "Jogadores da partida: ${game.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            // Verificar se todos os jogos foram completados
            val totalGames = game.rounds.sumOf { round ->
                (if (round.game1.isCompleted) 1 else 0) + (if (round.game2.isCompleted) 1 else 0)
            }
            Log.d("GameViewModel", "Total de jogos completados: $totalGames de 2")
            
            // Processar pontos da última rodada se estiver completa
            if (currentRound.isCompleted) {
                Log.d("GameViewModel", "Processando pontos da última rodada (${currentRound.roundNumber})...")
                processRoundPoints(currentRound)
                Log.d("GameViewModel", "Pontos da última rodada processados!")
                Log.d("GameViewModel", "Jogadores após processar última rodada: ${_currentGame.value?.players?.map { "${it.name}: ${it.totalPoints} pontos" }}")
            } else {
                Log.d("GameViewModel", "Última rodada não está completa, não processando pontos")
            }
            
            // Calcular vencedor
            val winner = _currentGame.value?.players?.maxByOrNull { it.totalPoints }
            Log.d("GameViewModel", "Vencedor da partida: ${winner?.name} com ${winner?.totalPoints} pontos")
            
            val finishedGame = _currentGame.value?.copy(
                isFinished = true,
                winner = winner
            ) ?: return
            
            // Adicionar à lista de jogos finalizados
            val updatedFinishedGames = _finishedGames.value.toMutableList()
            updatedFinishedGames.add(finishedGame)
            _finishedGames.value = updatedFinishedGames
            
            // Somar pontos da partida para a pontuação geral dos jogadores
            Log.d("GameViewModel", "=== SOMANDO PONTOS DA PARTIDA PARA PONTUAÇÃO GERAL ===")
            _currentGame.value?.players?.forEach { gamePlayer ->
                Log.d("GameViewModel", "Processando jogador: ${gamePlayer.name} com ${gamePlayer.totalPoints} pontos da partida")
                
                // Buscar o jogador na lista de jogadores salvos
                val currentList = _savedPlayers.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { 
                    it.name.trim().equals(gamePlayer.name.trim(), ignoreCase = true) 
                }
                
                if (existingIndex >= 0) {
                    // Jogador já existe - somar pontos da partida
                    val existingPlayer = currentList[existingIndex]
                    val updatedPlayer = existingPlayer.copy(
                        totalPoints = existingPlayer.totalPoints + gamePlayer.totalPoints,
                        gamesPlayed = existingPlayer.gamesPlayed + 1
                    )
                    
                    Log.d("GameViewModel", "Jogador existente: ${existingPlayer.name}")
                    Log.d("GameViewModel", "Pontos antigos: ${existingPlayer.totalPoints}, pontos da partida: ${gamePlayer.totalPoints}, total: ${updatedPlayer.totalPoints}")
                    
                    currentList[existingIndex] = updatedPlayer
                } else {
                    // Novo jogador - adicionar com pontos da partida
                    Log.d("GameViewModel", "Novo jogador: ${gamePlayer.name}")
                    currentList.add(gamePlayer)
                }
                
                // Atualizar a lista de jogadores salvos
                _savedPlayers.value = currentList
                savePlayersToPreferences(currentList)
            }
            
            // Atualizar estatísticas
            updateStatistics()
            saveFinishedGamesToPreferences(updatedFinishedGames)
            
            // NÃO LIMPAR JOGO ATUAL - MANTER PARA RANQUEAMENTO FINAL
            // clearCurrentGame() - REMOVIDO PARA NÃO QUEBRAR RANQUEAMENTO
            
            Log.d("GameViewModel", "Torneio finalizado com sucesso!")
            Log.d("GameViewModel", "Pontuação geral atualizada: ${_savedPlayers.value.map { "${it.name}: ${it.totalPoints} pontos" }}")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO FINALIZAR TORNEIO: ${e.message}", e)
        }
    }
    
    // Marcar jogo como finalizado
    fun markGameAsFinished(gameCode: String) {
        try {
            Log.d("GameViewModel", "=== MARCANDO JOGO COMO FINALIZADO ===")
            Log.d("GameViewModel", "GameCode: $gameCode")
            
            val currentGame = _currentGame.value
            if (currentGame != null && currentGame.gameCode == gameCode) {
                val updatedGame = currentGame.copy(isFinished = true)
                _currentGame.value = updatedGame
                
                Log.d("GameViewModel", "Jogo marcado como finalizado: ${updatedGame.isFinished}")
            } else {
                Log.d("GameViewModel", "Jogo não encontrado ou gameCode não confere")
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO MARCAR JOGO COMO FINALIZADO: ${e.message}", e)
        }
    }
    
    // Gerar rodadas automaticamente seguindo a tabela específica
    private fun generateRounds(game: Game) {
        try {
            Log.d("GameViewModel", "Gerando rodadas seguindo tabela específica para ${game.players.size} jogadores")
            Log.d("GameViewModel", "Jogadores antes de gerar rodadas: ${game.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            
            // Mapear jogadores por posição (1-8)
            val playersByPosition = game.players.withIndex().associate { (index, player) ->
                (index + 1) to player
            }
            
            Log.d("GameViewModel", "Jogadores mapeados: ${playersByPosition.map { "${it.key}-${it.value.name}: ${it.value.totalPoints} pontos" }}")
            
            // Tabela específica de sorteio (posições dos jogadores) - 7 RODADAS COMPLETAS
            val roundSchedule = listOf(
                // RODADA 1: 1&5 vs 7&8, 2&3 vs 4&6
                listOf(
                    listOf(listOf(1, 5), listOf(7, 8)),
                    listOf(listOf(2, 3), listOf(4, 6))
                ),
                // RODADA 2: 1&2 vs 3&4, 5&6 vs 7&8
                listOf(
                    listOf(listOf(1, 2), listOf(3, 4)),
                    listOf(listOf(5, 6), listOf(7, 8))
                ),
                // RODADA 3: 1&3 vs 2&4, 5&7 vs 6&8
                listOf(
                    listOf(listOf(1, 3), listOf(2, 4)),
                    listOf(listOf(5, 7), listOf(6, 8))
                ),
                // RODADA 4: 1&4 vs 2&3, 5&8 vs 6&7
                listOf(
                    listOf(listOf(1, 4), listOf(2, 3)),
                    listOf(listOf(5, 8), listOf(6, 7))
                ),
                // RODADA 5: 1&6 vs 2&5, 3&8 vs 4&7
                listOf(
                    listOf(listOf(1, 6), listOf(2, 5)),
                    listOf(listOf(3, 8), listOf(4, 7))
                ),
                // RODADA 6: 1&7 vs 2&6, 3&5 vs 4&8
                listOf(
                    listOf(listOf(1, 7), listOf(2, 6)),
                    listOf(listOf(3, 5), listOf(4, 8))
                ),
                // RODADA 7: 1&8 vs 2&7, 3&6 vs 4&5
                listOf(
                    listOf(listOf(1, 8), listOf(2, 7)),
                    listOf(listOf(3, 6), listOf(4, 5))
                )
            )
            
            val rounds = mutableListOf<Round>()
            
            // Gerar rodadas baseadas na tabela
            roundSchedule.forEachIndexed { roundIndex, games ->
                val roundNumber = roundIndex + 1
                
                // Cada rodada tem 2 jogos
                val game1Positions = games[0]
                val game2Positions = games[1]
                
                val game1Player1 = playersByPosition[game1Positions[0][0]]
                val game1Player2 = playersByPosition[game1Positions[0][1]]
                val game1Player3 = playersByPosition[game1Positions[1][0]]
                val game1Player4 = playersByPosition[game1Positions[1][1]]
                
                val game2Player1 = playersByPosition[game2Positions[0][0]]
                val game2Player2 = playersByPosition[game2Positions[0][1]]
                val game2Player3 = playersByPosition[game2Positions[1][0]]
                val game2Player4 = playersByPosition[game2Positions[1][1]]
                
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
                    
                    Log.d("GameViewModel", "Rodada $roundNumber criada:")
                    Log.d("GameViewModel", "  Jogo 1: ${game1Player1.name} & ${game1Player2.name} vs ${game1Player3.name} & ${game1Player4.name}")
                    Log.d("GameViewModel", "  Jogo 2: ${game2Player1.name} & ${game2Player2.name} vs ${game2Player3.name} & ${game2Player4.name}")
                } else {
                    Log.e("GameViewModel", "Erro: Jogador não encontrado para rodada $roundNumber")
                }
            }
            
            Log.d("GameViewModel", "Rodadas geradas: ${rounds.size}")
            Log.d("GameViewModel", "Jogadores após gerar rodadas: ${game.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            _currentGame.value = game.copy(rounds = rounds)
            Log.d("GameViewModel", "Jogo atualizado com rodadas. Jogadores finais: ${_currentGame.value?.players?.map { "${it.name}: ${it.totalPoints} pontos" }}")
        } catch (e: Exception) {
            Log.e("GameViewModel", "Erro ao gerar rodadas", e)
        }
    }
    
    // Atualizar estatísticas
    private fun updateStatistics() {
        val finishedGames = _finishedGames.value
        val totalRounds = finishedGames.sumOf { it.rounds.size }
        val uniquePlayers = finishedGames.flatMap { it.players }.distinctBy { it.id }.size
        
        _statistics.value = GameStatistics(
            gamesPlayed = finishedGames.size,
            roundsPlayed = totalRounds,
            uniquePlayers = uniquePlayers
        )
    }
    
    // Carregar jogadores salvos
    private fun loadSavedPlayers() {
        try {
            Log.d("GameViewModel", "=== CARREGANDO JOGADORES SALVOS ===")
            
            context?.let { context ->
                Log.d("GameViewModel", "Contexto disponível, acessando SharedPreferences")
                val sharedPreferences: SharedPreferences = context.getSharedPreferences("Super8Prefs", Context.MODE_PRIVATE)
                val jsonPlayers = sharedPreferences.getString("savedPlayers", null)
                
                Log.d("GameViewModel", "JSON jogadores: $jsonPlayers")
                
                if (jsonPlayers != null) {
                    val type = object : TypeToken<List<Player>>() {}.type
                    val players: List<Player> = Gson().fromJson(jsonPlayers, type)
                    persistentPlayers.clear()
                    persistentPlayers.addAll(players)
                    _savedPlayers.value = persistentPlayers.toList()
                    Log.d("GameViewModel", "Jogadores carregados de SharedPreferences: ${persistentPlayers.size}")
                } else {
                    Log.d("GameViewModel", "Nenhum jogador encontrado em SharedPreferences.")
                }
            } ?: run {
                Log.d("GameViewModel", "Contexto não disponível para carregar jogadores")
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO CARREGAR JOGADORES: ${e.message}", e)
        }
    }
    
    // Carregar jogos finalizados
    private fun loadFinishedGames() {
        try {
            Log.d("GameViewModel", "=== CARREGANDO JOGOS FINALIZADOS ===")
            
            context?.let { context ->
                val sharedPreferences: SharedPreferences = context.getSharedPreferences("Super8Prefs", Context.MODE_PRIVATE)
                val jsonGames = sharedPreferences.getString("finishedGames", "[]")
                
                if (jsonGames != null && jsonGames.isNotEmpty()) {
                    val gson = Gson()
                    val type = object : TypeToken<List<Game>>() {}.type
                    val games = gson.fromJson<List<Game>>(jsonGames, type) ?: emptyList()
                    
                    _finishedGames.value = games
                    Log.d("GameViewModel", "Jogos finalizados carregados: ${games.size}")
                    games.forEach { game ->
                        Log.d("GameViewModel", "Jogo: ${game.gameCode} - Finalizado: ${game.isFinished}")
                    }
                } else {
                    Log.d("GameViewModel", "Nenhum jogo finalizado encontrado")
                    _finishedGames.value = emptyList()
                }
            } ?: run {
                Log.d("GameViewModel", "Contexto não disponível, usando lista vazia")
                _finishedGames.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO CARREGAR JOGOS FINALIZADOS: ${e.message}", e)
            _finishedGames.value = emptyList()
        }
    }
    
    // Salvar jogadores nas preferências (simulado)
    private fun savePlayersToPreferences(players: List<Player>) {
        try {
            Log.d("GameViewModel", "=== SALVANDO JOGADORES ===")
            Log.d("GameViewModel", "Jogadores para salvar: ${players.map { it.name }}")
            
            context?.let { context ->
                val sharedPreferences: SharedPreferences = context.getSharedPreferences("Super8Prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                val gson = Gson()
                val jsonPlayers = gson.toJson(players)
                editor.putString("savedPlayers", jsonPlayers)
                editor.apply()
                Log.d("GameViewModel", "Jogadores salvos com sucesso!")
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO SALVAR JOGADORES: ${e.message}", e)
        }
    }
    
    // Salvar jogos finalizados nas preferências
    private fun saveFinishedGamesToPreferences(games: List<Game>) {
        try {
            Log.d("GameViewModel", "=== SALVANDO JOGOS FINALIZADOS ===")
            Log.d("GameViewModel", "Jogos para salvar: ${games.size}")
            Log.d("GameViewModel", "Context: ${context != null}")
            
            context?.let { context ->
                val sharedPreferences: SharedPreferences = context.getSharedPreferences("Super8Prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                val gson = Gson()
                val jsonGames = gson.toJson(games)
                
                Log.d("GameViewModel", "JSON gerado: ${jsonGames.take(100)}...")
                
                editor.putString("finishedGames", jsonGames)
                val success = editor.commit() // Usar commit() em vez de apply() para garantir
                
                Log.d("GameViewModel", "Jogos finalizados salvos com sucesso! Success: $success")
                
                // Verificar se salvou
                val savedJson = sharedPreferences.getString("finishedGames", "")
                Log.d("GameViewModel", "Verificação - JSON salvo: ${savedJson?.take(100)}...")
            } ?: run {
                Log.e("GameViewModel", "Contexto não disponível para salvar jogos!")
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO SALVAR JOGOS FINALIZADOS: ${e.message}", e)
        }
    }

    // Atualizar pontuação de jogador (para desempate)
    fun updatePlayerScore(playerId: String, newScore: Int) {
        try {
            Log.d("GameViewModel", "=== ATUALIZANDO PONTUAÇÃO DO JOGADOR ===")
            val game = _currentGame.value ?: return
            
            val updatedPlayers = game.players.toMutableList()
            val playerIndex = updatedPlayers.indexOfFirst { it.id == playerId }
            
            if (playerIndex >= 0) {
                val oldScore = updatedPlayers[playerIndex].totalPoints
                updatedPlayers[playerIndex] = updatedPlayers[playerIndex].copy(totalPoints = newScore)
                
                Log.d("GameViewModel", "Pontuação atualizada: ${updatedPlayers[playerIndex].name} $oldScore → $newScore")
                
                _currentGame.value = game.copy(players = updatedPlayers)
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO ATUALIZAR PONTUAÇÃO: ${e.message}", e)
        }
    }

    // Processar pontos de uma rodada completa
    private fun processRoundPoints(round: Round) {
        try {
            val game = _currentGame.value ?: return
            val updatedPlayers = game.players.toMutableList()
            
            Log.d("GameViewModel", "=== PROCESSANDO PONTOS DA RODADA ${round.roundNumber} ===")
            
            // Processar pontos do Jogo 1
            if (round.game1.isCompleted) {
                Log.d("GameViewModel", "Processando Jogo 1: ${round.game1.pair1.first.name} & ${round.game1.pair1.second.name} vs ${round.game1.pair2.first.name} & ${round.game1.pair2.second.name}")
                Log.d("GameViewModel", "Placar Jogo 1: ${round.game1.pair1Score} x ${round.game1.pair2Score}")
                
                // Atualizar pontos da dupla 1 (Jogo 1)
                val player1Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair1.first.id }
                val player2Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair1.second.id }
                
                if (player1Index >= 0) {
                    val oldPoints = updatedPlayers[player1Index].totalPoints
                    updatedPlayers[player1Index] = updatedPlayers[player1Index].copy(totalPoints = oldPoints + round.game1.pair1Score)
                    Log.d("GameViewModel", "${updatedPlayers[player1Index].name}: $oldPoints → ${updatedPlayers[player1Index].totalPoints} (+${round.game1.pair1Score})")
                }
                
                if (player2Index >= 0) {
                    val oldPoints = updatedPlayers[player2Index].totalPoints
                    updatedPlayers[player2Index] = updatedPlayers[player2Index].copy(totalPoints = oldPoints + round.game1.pair1Score)
                    Log.d("GameViewModel", "${updatedPlayers[player2Index].name}: $oldPoints → ${updatedPlayers[player2Index].totalPoints} (+${round.game1.pair1Score})")
                }
                
                // Atualizar pontos da dupla 2 (Jogo 1)
                val player3Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair2.first.id }
                val player4Index = updatedPlayers.indexOfFirst { it.id == round.game1.pair2.second.id }
                
                if (player3Index >= 0) {
                    val oldPoints = updatedPlayers[player3Index].totalPoints
                    updatedPlayers[player3Index] = updatedPlayers[player3Index].copy(totalPoints = oldPoints + round.game1.pair2Score)
                    Log.d("GameViewModel", "${updatedPlayers[player3Index].name}: $oldPoints → ${updatedPlayers[player3Index].totalPoints} (+${round.game1.pair2Score})")
                }
                
                if (player4Index >= 0) {
                    val oldPoints = updatedPlayers[player4Index].totalPoints
                    updatedPlayers[player4Index] = updatedPlayers[player4Index].copy(totalPoints = oldPoints + round.game1.pair2Score)
                    Log.d("GameViewModel", "${updatedPlayers[player4Index].name}: $oldPoints → ${updatedPlayers[player4Index].totalPoints} (+${round.game1.pair2Score})")
                }
            }
            
            // Processar pontos do Jogo 2
            if (round.game2.isCompleted) {
                Log.d("GameViewModel", "Processando Jogo 2: ${round.game2.pair1.first.name} & ${round.game2.pair1.second.name} vs ${round.game2.pair2.first.name} & ${round.game2.pair2.second.name}")
                Log.d("GameViewModel", "Placar Jogo 2: ${round.game2.pair1Score} x ${round.game2.pair2Score}")
                
                // Atualizar pontos da dupla 1 (Jogo 2)
                val player1Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair1.first.id }
                val player2Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair1.second.id }
                
                if (player1Index >= 0) {
                    val oldPoints = updatedPlayers[player1Index].totalPoints
                    updatedPlayers[player1Index] = updatedPlayers[player1Index].copy(totalPoints = oldPoints + round.game2.pair1Score)
                    Log.d("GameViewModel", "${updatedPlayers[player1Index].name}: $oldPoints → ${updatedPlayers[player1Index].totalPoints} (+${round.game2.pair1Score})")
                }
                
                if (player2Index >= 0) {
                    val oldPoints = updatedPlayers[player2Index].totalPoints
                    updatedPlayers[player2Index] = updatedPlayers[player2Index].copy(totalPoints = oldPoints + round.game2.pair1Score)
                    Log.d("GameViewModel", "${updatedPlayers[player2Index].name}: $oldPoints → ${updatedPlayers[player2Index].totalPoints} (+${round.game2.pair1Score})")
                }
                
                // Atualizar pontos da dupla 2 (Jogo 2)
                val player3Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair2.first.id }
                val player4Index = updatedPlayers.indexOfFirst { it.id == round.game2.pair2.second.id }
                
                if (player3Index >= 0) {
                    val oldPoints = updatedPlayers[player3Index].totalPoints
                    updatedPlayers[player3Index] = updatedPlayers[player3Index].copy(totalPoints = oldPoints + round.game2.pair2Score)
                    Log.d("GameViewModel", "${updatedPlayers[player3Index].name}: $oldPoints → ${updatedPlayers[player3Index].totalPoints} (+${round.game2.pair2Score})")
                }
                
                if (player4Index >= 0) {
                    val oldPoints = updatedPlayers[player4Index].totalPoints
                    updatedPlayers[player4Index] = updatedPlayers[player4Index].copy(totalPoints = oldPoints + round.game2.pair2Score)
                    Log.d("GameViewModel", "${updatedPlayers[player4Index].name}: $oldPoints → ${updatedPlayers[player4Index].totalPoints} (+${round.game2.pair2Score})")
                }
            }
            
            _currentGame.value = game.copy(players = updatedPlayers)
            Log.d("GameViewModel", "Pontos da rodada ${round.roundNumber} processados com sucesso!")
            Log.d("GameViewModel", "Estado final dos jogadores: ${updatedPlayers.map { "${it.name}: ${it.totalPoints}" }}")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO PROCESSAR PONTOS DA RODADA: ${e.message}", e)
        }
    }

    // Função para resetar o jogo
    fun resetGame() {
        try {
            Log.d("GameViewModel", "=== RESETANDO JOGO ===")
            _currentGame.value = null
            _currentRound.value = null
            Log.d("GameViewModel", "Jogo resetado com sucesso")
        } catch (e: Exception) {
            Log.e("GameViewModel", "Erro ao resetar jogo", e)
        }
    }

    private fun saveGame(game: Game) {
        try {
            Log.d("GameViewModel", "Salvando jogo: ${game.gameCode}")
            // Implementar salvamento do jogo
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO SALVAR JOGO: ${e.message}", e)
        }
    }
    
    private fun addToHistory(game: Game) {
        try {
            Log.d("GameViewModel", "Adicionando jogo ao histórico: ${game.gameCode}")
            val updatedHistory = _finishedGames.value.toMutableList()
            updatedHistory.add(game)
            _finishedGames.value = updatedHistory
            saveFinishedGamesToPreferences(updatedHistory)
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO ADICIONAR AO HISTÓRICO: ${e.message}", e)
        }
    }
    
    // Salvar jogo no histórico
    fun saveGameToHistory(game: Game) {
        try {
            Log.d("GameViewModel", "=== SALVANDO JOGO NO HISTÓRICO ===")
            Log.d("GameViewModel", "Jogo: ${game.gameCode}")
            Log.d("GameViewModel", "Jogadores: ${game.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            Log.d("GameViewModel", "Context: ${context != null}")
            
            // Marcar o jogo como finalizado
            val finishedGame = game.copy(isFinished = true)
            
            // Atualizar o jogo atual como finalizado
            _currentGame.value = finishedGame
            
            // Adicionar ao histórico de jogos finalizados
            val updatedFinishedGames = _finishedGames.value.toMutableList()
            updatedFinishedGames.add(finishedGame)
            _finishedGames.value = updatedFinishedGames
            
            Log.d("GameViewModel", "Jogo adicionado à lista. Total: ${updatedFinishedGames.size}")
            
            // Salvar nas preferências
            saveFinishedGamesToPreferences(updatedFinishedGames)
            
            Log.d("GameViewModel", "Jogo salvo no histórico e marcado como finalizado!")
            Log.d("GameViewModel", "Edições agora estão bloqueadas!")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO SALVAR JOGO NO HISTÓRICO: ${e.message}", e)
        }
    }
    
    // Adicionar pontos para ranking geral
    fun addPointsToGeneralRanking(player: Player) {
        try {
            Log.d("GameViewModel", "=== ADICIONANDO PONTOS AO RANKING GERAL ===")
            Log.d("GameViewModel", "Jogador: ${player.name} com ${player.totalPoints} pontos")
            
            val currentList = _savedPlayers.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { 
                it.name.trim().equals(player.name.trim(), ignoreCase = true) 
            }
            
            if (existingIndex >= 0) {
                // Jogador já existe - somar pontos
                val existingPlayer = currentList[existingIndex]
                val updatedPlayer = existingPlayer.copy(
                    totalPoints = existingPlayer.totalPoints + player.totalPoints,
                    gamesPlayed = existingPlayer.gamesPlayed + 1
                )
                
                Log.d("GameViewModel", "Jogador existente: ${existingPlayer.name}")
                Log.d("GameViewModel", "Pontos antigos: ${existingPlayer.totalPoints}, novos: ${player.totalPoints}, total: ${updatedPlayer.totalPoints}")
                
                currentList[existingIndex] = updatedPlayer
            } else {
                // Novo jogador
                Log.d("GameViewModel", "Novo jogador: ${player.name}")
                currentList.add(player.copy(gamesPlayed = 1))
            }
            
            _savedPlayers.value = currentList
            savePlayersToPreferences(currentList)
            
            Log.d("GameViewModel", "Pontos adicionados ao ranking geral com sucesso!")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO ADICIONAR PONTOS AO RANKING GERAL: ${e.message}", e)
        }
    }

    // Atualizar rodadas do jogo atual
    fun updateGameRounds(updatedRounds: List<Round>) {
        try {
            Log.d("GameViewModel", "=== ATUALIZANDO RODADAS DO JOGO ===")
            Log.d("GameViewModel", "Rodadas recebidas: ${updatedRounds.size}")
            
            val currentGame = _currentGame.value
            if (currentGame != null) {
                val updatedGame = currentGame.copy(rounds = updatedRounds)
                _currentGame.value = updatedGame
                
                // Recalcular pontos dos jogadores
                recalculatePlayerPoints(updatedGame)
                
                Log.d("GameViewModel", "Rodadas atualizadas com sucesso!")
                Log.d("GameViewModel", "Jogadores após recálculo: ${updatedGame.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO ATUALIZAR RODADAS: ${e.message}", e)
        }
    }
    
    // Recalcular pontos dos jogadores baseado nas rodadas
    private fun recalculatePlayerPoints(game: Game) {
        try {
            Log.d("GameViewModel", "=== RECALCULANDO PONTOS DOS JOGADORES ===")
            
            // Zerar pontos de todos os jogadores
            val updatedPlayers = game.players.map { it.copy(totalPoints = 0) }.toMutableList()
            
            // Calcular pontos baseado em todos os jogos completados
            game.rounds.forEach { round ->
                // Jogo 1
                if (round.game1.isCompleted) {
                    updatePlayerPointsFromGame(round.game1, updatedPlayers)
                }
                
                // Jogo 2
                if (round.game2.isCompleted) {
                    updatePlayerPointsFromGame(round.game2, updatedPlayers)
                }
            }
            
            // Atualizar o jogo com os novos pontos
            val updatedGame = game.copy(players = updatedPlayers)
            _currentGame.value = updatedGame
            
            Log.d("GameViewModel", "Pontos recalculados com sucesso!")
            Log.d("GameViewModel", "Jogadores finais: ${updatedPlayers.map { "${it.name}: ${it.totalPoints} pontos" }}")
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO RECALCULAR PONTOS: ${e.message}", e)
        }
    }
    
    // Atualizar pontos de um jogo específico
    private fun updatePlayerPointsFromGame(gameMatch: GameMatch, players: MutableList<Player>) {
        try {
            // Encontrar e atualizar jogadores da dupla 1
            players.find { it.name == gameMatch.pair1.first.name }?.let { player ->
                val index = players.indexOf(player)
                players[index] = player.copy(totalPoints = player.totalPoints + gameMatch.pair1Score)
            }
            
            players.find { it.name == gameMatch.pair1.second.name }?.let { player ->
                val index = players.indexOf(player)
                players[index] = player.copy(totalPoints = player.totalPoints + gameMatch.pair1Score)
            }
            
            // Encontrar e atualizar jogadores da dupla 2
            players.find { it.name == gameMatch.pair2.first.name }?.let { player ->
                val index = players.indexOf(player)
                players[index] = player.copy(totalPoints = player.totalPoints + gameMatch.pair2Score)
            }
            
            players.find { it.name == gameMatch.pair2.second.name }?.let { player ->
                val index = players.indexOf(player)
                players[index] = player.copy(totalPoints = player.totalPoints + gameMatch.pair2Score)
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO ATUALIZAR PONTOS DO JOGO: ${e.message}", e)
        }
    }

    // Função para gerar código único
    fun generateUniqueGameCode(): String {
        var code: String
        do {
            code = generateRandomCode()
        } while (gameCodeExists(code))
        Log.d("GameViewModel", "Código único gerado: $code")
        return code
    }
    
    // Função para gerar código aleatório
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
    
    // Função para salvar jogo atual
    fun saveCurrentGame() {
        try {
            Log.d("GameViewModel", "=== SALVANDO JOGO ATUAL ===")
            val currentGame = _currentGame.value
            if (currentGame != null && !currentGame.isFinished) {
                Log.d("GameViewModel", "Salvando jogo: ${currentGame.gameCode}")
                
                context?.let { context ->
                    val sharedPreferences: SharedPreferences = context.getSharedPreferences("Super8Prefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val gson = Gson()
                    val jsonGame = gson.toJson(currentGame)
                    editor.putString("currentGame", jsonGame)
                    editor.apply()
                    Log.d("GameViewModel", "Jogo atual salvo com sucesso!")
                }
            } else {
                Log.d("GameViewModel", "Nenhum jogo atual para salvar ou jogo já finalizado")
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO SALVAR JOGO ATUAL: ${e.message}", e)
        }
    }
    
    // Função para carregar jogo atual
    fun loadCurrentGame(): Game? {
        try {
            Log.d("GameViewModel", "=== CARREGANDO JOGO ATUAL ===")
            
            context?.let { context ->
                val sharedPreferences: SharedPreferences = context.getSharedPreferences("Super8Prefs", Context.MODE_PRIVATE)
                val jsonGame = sharedPreferences.getString("currentGame", null)
                
                if (jsonGame != null) {
                    val gson = Gson()
                    val game = gson.fromJson<Game>(jsonGame, Game::class.java)
                    
                    if (game != null && !game.isFinished) {
                        Log.d("GameViewModel", "Jogo atual carregado: ${game.gameCode}")
                        _currentGame.value = game
                        _currentRound.value = game.rounds.getOrNull(game.currentRound - 1)
                        return game
                    } else {
                        Log.d("GameViewModel", "Jogo carregado está finalizado, mantendo para ranqueamento...")
                        // NÃO LIMPAR - MANTER PARA RANQUEAMENTO FINAL
                        // clearCurrentGame() - REMOVIDO PARA NÃO QUEBRAR RANQUEAMENTO
                    }
                } else {
                    Log.d("GameViewModel", "Nenhum jogo atual encontrado")
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO CARREGAR JOGO ATUAL: ${e.message}", e)
            return null
        }
    }
    
    // Função para limpar jogo atual
    fun clearCurrentGame() {
        try {
            Log.d("GameViewModel", "=== LIMPANDO JOGO ATUAL ===")
            
            context?.let { context ->
                val sharedPreferences: SharedPreferences = context.getSharedPreferences("Super8Prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.remove("currentGame")
                editor.apply()
                Log.d("GameViewModel", "Jogo atual removido das preferências")
            }
            
            _currentGame.value = null
            _currentRound.value = null
        } catch (e: Exception) {
            Log.e("GameViewModel", "ERRO AO LIMPAR JOGO ATUAL: ${e.message}", e)
        }
    }
    
    // Função para verificar se código já existe (CORRIGIDA)
    fun gameCodeExists(code: String): Boolean {
        // Verificar em jogos finalizados
        val existsInFinished = _finishedGames.value.any { it.gameCode == code }
        
        // Verificar no jogo atual
        val existsInCurrent = _currentGame.value?.gameCode == code
        
        // Verificar no jogo salvo (carregar se necessário)
        var existsInSaved = false
        if (!existsInCurrent && !existsInFinished) {
            val savedGame = loadCurrentGame()
            existsInSaved = savedGame?.gameCode == code
        }
        
        val exists = existsInFinished || existsInCurrent || existsInSaved
        Log.d("GameViewModel", "Verificando código $code - Existe em finalizados: $existsInFinished, atual: $existsInCurrent, salvo: $existsInSaved, total: $exists")
        return exists
    }
    
    // Função para carregar jogo por código (CORRIGIDA)
    fun loadGameByCode(code: String): Game? {
        try {
            Log.d("GameViewModel", "Tentando carregar jogo com código: $code")
            
            // Verificar se é o jogo atual (não finalizado)
            val currentGame = _currentGame.value
            if (currentGame?.gameCode == code && !currentGame.isFinished) {
                Log.d("GameViewModel", "Jogo encontrado: jogo atual")
                return currentGame
            }
            
            // Verificar em jogos finalizados (não deve permitir)
            val finishedGame = _finishedGames.value.find { it.gameCode == code }
            if (finishedGame != null) {
                Log.d("GameViewModel", "Código encontrado em jogo finalizado - NÃO PERMITIDO")
                return null
            }
            
            // Tentar carregar do jogo salvo
            val savedGame = loadCurrentGame()
            if (savedGame?.gameCode == code && !savedGame.isFinished) {
                Log.d("GameViewModel", "Jogo encontrado: jogo salvo")
                return savedGame
            }
            
            Log.d("GameViewModel", "Código não encontrado")
            return null
        } catch (e: Exception) {
            Log.e("GameViewModel", "Erro ao carregar jogo por código", e)
            return null
        }
    }
    
    // Função para validar entrada em jogo (CORRIGIDA)
    fun validateGameCode(code: String): GameCodeValidation {
        return when {
            code.isBlank() -> GameCodeValidation.EMPTY
            _finishedGames.value.any { it.gameCode == code } -> GameCodeValidation.FINISHED
            _currentGame.value?.gameCode == code -> GameCodeValidation.VALID
            loadCurrentGame()?.gameCode == code -> GameCodeValidation.VALID
            else -> GameCodeValidation.NOT_FOUND
        }
    }
    
    // Enum para validação de código
    enum class GameCodeValidation {
        EMPTY,      // Código vazio
        NOT_FOUND,  // Código não encontrado
        FINISHED,   // Jogo já finalizado
        VALID       // Código válido
    }
}

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val roundsPlayed: Int = 0,
    val uniquePlayers: Int = 0
) 