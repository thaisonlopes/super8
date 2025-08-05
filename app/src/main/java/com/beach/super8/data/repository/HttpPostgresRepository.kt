package com.beach.super8.data.repository

import android.util.Log
import com.beach.super8.data.entities.PlayerEntity
import com.beach.super8.data.entities.GameEntity
import com.beach.super8.data.entities.MatchEntity
import com.beach.super8.data.entities.PontuacaoJogoEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class HttpPostgresRepository {
    companion object {
        private const val TAG = "HttpPostgresRepository"
        // Use 10.0.2.2 para Android Emulator, ou 192.168.0.111 para dispositivo físico
        private const val BASE_URL = "http://192.168.0.111:3000/api"
        private val gson = Gson()
    }

    suspend fun insertPlayer(player: PlayerEntity): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Inserindo jogador via HTTP: ${player.nome}")
            
            val url = URL("$BASE_URL/players")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // Adicionar timeout para evitar travamentos
            connection.connectTimeout = 10000 // 10 segundos
            connection.readTimeout = 10000 // 10 segundos
            
            val jsonData = gson.toJson(mapOf(
                "nome" to player.nome
            ))
            
            Log.d(TAG, "Enviando dados: $jsonData")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonData)
            }
            
            Log.d(TAG, "Fazendo conexão HTTP...")
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                Log.d(TAG, "Lendo resposta...")
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta: $response")
                Log.d(TAG, "✅ Jogador inserido com sucesso via HTTP")
                true
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                // Tentar ler o erro
                try {
                    val errorResponse = BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                        reader.readText()
                    }
                    Log.e(TAG, "Erro response: $errorResponse")
                } catch (e: Exception) {
                    Log.e(TAG, "Não foi possível ler erro response: ${e.message}")
                }
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inserir jogador via HTTP: ${e.message}", e)
            Log.e(TAG, "Stack trace completo:", e)
            false
        }
    }

    suspend fun getAllPlayers(): List<PlayerEntity> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "=== CARREGANDO JOGADORES VIA HTTP ===")
            Log.d(TAG, "URL: $BASE_URL/players")
            
            val url = URL("$BASE_URL/players")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            // Aumentar timeout para 30 segundos
            connection.connectTimeout = 30000 // 30 segundos
            connection.readTimeout = 30000 // 30 segundos
            
            Log.d(TAG, "Fazendo conexão HTTP...")
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Lendo resposta...")
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta completa: $response")
                
                val jsonResponse = gson.fromJson(response, Map::class.java)
                Log.d(TAG, "JSON parseado: $jsonResponse")
                
                val playersData = jsonResponse["players"] as? List<*>
                Log.d(TAG, "Players data: $playersData")
                Log.d(TAG, "Players data size: ${playersData?.size}")
                
                val players = playersData?.mapNotNull { playerData ->
                    Log.d(TAG, "Processando playerData: $playerData")
                    val playerMap = playerData as? Map<*, *>
                    if (playerMap != null) {
                        val playerId = playerMap["id"]
                        val playerNome = playerMap["nome"]
                        Log.d(TAG, "Player ID: $playerId, Nome: $playerNome")
                        
                        val player = PlayerEntity(
                            id = (playerId as? Number)?.toInt() ?: 0,
                            nome = playerNome as String,
                            dataCriacao = Date(), // Simplificado
                            dataAtualizacao = Date() // Simplificado
                        )
                        Log.d(TAG, "Jogador parseado: ${player.id} - ${player.nome}")
                        player
                    } else {
                        Log.w(TAG, "Player data inválido: $playerData")
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "✅ Jogadores carregados com sucesso via HTTP: ${players.size}")
                Log.d(TAG, "Jogadores: ${players.map { "${it.id} - ${it.nome}" }}")
                players
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                // Tentar ler o erro
                try {
                    val errorResponse = BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                        reader.readText()
                    }
                    Log.e(TAG, "Erro response: $errorResponse")
                } catch (e: Exception) {
                    Log.e(TAG, "Não foi possível ler erro response: ${e.message}")
                }
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao carregar jogadores via HTTP: ${e.message}", e)
            Log.e(TAG, "Stack trace completo:", e)
            emptyList()
        }
    }

    suspend fun insertGame(game: GameEntity): Int? = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Inserindo jogo via HTTP: ${game.codigoJogo}")
            
            val url = URL("$BASE_URL/games")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val jsonData = gson.toJson(mapOf(
                "codigo_jogo" to game.codigoJogo,
                "finalizado" to game.finalizado
            ))
            
            Log.d(TAG, "Enviando dados do jogo: $jsonData")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonData)
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta: $response")
                
                // Parsear a resposta para obter o ID do jogo
                val jsonResponse = gson.fromJson(response, Map::class.java)
                val gameData = jsonResponse["game"] as? Map<*, *>
                val gameId = gameData?.get("id") as? Number
                
                Log.d(TAG, "✅ Jogo inserido com sucesso via HTTP, ID: $gameId")
                gameId?.toInt()
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inserir jogo via HTTP: ${e.message}", e)
            null
        }
    }

    suspend fun getGameByCode(gameCode: String): GameEntity? = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Buscando jogo via HTTP: $gameCode")
            
            val url = URL("$BASE_URL/games/$gameCode")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta: $response")
                
                val jsonResponse = gson.fromJson(response, Map::class.java)
                val gameData = jsonResponse["game"]
                
                if (gameData != null) {
                    val gameMap = gameData as Map<*, *>
                    val game = GameEntity(
                        id = (gameMap["id"] as? Number)?.toInt() ?: 0,
                        codigoJogo = gameMap["codigo_jogo"] as String,
                        finalizado = gameMap["finalizado"] as Boolean,
                        dataCriacao = Date(), // Simplificado
                        dataAtualizacao = Date() // Simplificado
                    )
                    Log.d(TAG, "✅ Jogo encontrado via HTTP: ${game.codigoJogo}")
                    game
                } else {
                    Log.d(TAG, "✅ Jogo não encontrado via HTTP")
                    null
                }
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar jogo via HTTP: ${e.message}", e)
            null
        }
    }

    suspend fun insertMatch(match: MatchEntity): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Inserindo partida via HTTP: ${match.idJogo} - Rodada ${match.rodada}")
            
            val url = URL("$BASE_URL/matches")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val jsonData = gson.toJson(mapOf(
                "id_jogo" to match.idJogo,
                "rodada" to match.rodada,
                "id_jogador1" to match.idJogador1,
                "id_jogador2" to match.idJogador2,
                "pontuacao_jogador1" to match.pontuacaoJogador1,
                "pontuacao_jogador2" to match.pontuacaoJogador2,
                "finalizada" to match.finalizada
            ))
            
            Log.d(TAG, "Enviando dados da partida: $jsonData")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonData)
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta: $response")
                Log.d(TAG, "✅ Partida inserida com sucesso via HTTP")
                true
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inserir partida via HTTP: ${e.message}", e)
            false
        }
    }

    suspend fun updateMatch(gameId: Int, round: Int, match: MatchEntity): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Atualizando partida via HTTP: $gameId - Rodada $round")
            
            val url = URL("$BASE_URL/matches/$gameId/$round")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val jsonData = gson.toJson(mapOf(
                "id_jogador1" to match.idJogador1,
                "id_jogador2" to match.idJogador2,
                "pontuacao_jogador1" to match.pontuacaoJogador1,
                "pontuacao_jogador2" to match.pontuacaoJogador2,
                "finalizada" to match.finalizada
            ))
            
            Log.d(TAG, "Enviando dados da partida para atualização: $jsonData")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonData)
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta: $response")
                Log.d(TAG, "✅ Partida atualizada com sucesso via HTTP")
                true
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao atualizar partida via HTTP: ${e.message}", e)
            false
        }
    }

    suspend fun getGeneralRanking(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "=== CARREGANDO RANKING GERAL VIA HTTP ===")
            Log.d(TAG, "URL: $BASE_URL/ranking-geral")
            
            val url = URL("$BASE_URL/ranking-geral")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            Log.d(TAG, "Fazendo conexão HTTP...")
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Lendo resposta...")
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta completa: $response")
                
                val jsonResponse = gson.fromJson(response, Map::class.java)
                val rankingData = jsonResponse["ranking"] as? List<*>
                
                val ranking = rankingData?.mapNotNull { playerData ->
                    val playerMap = playerData as? Map<*, *>
                    if (playerMap != null) {
                        val id = (playerMap["id"] as? Number)?.toInt() ?: 0
                        val nome = (playerMap["nome"] as? String) ?: ""
                        val jogosJogados = when (val jogos = playerMap["jogos_jogados"]) {
                            is Number -> jogos.toInt()
                            is String -> jogos.toIntOrNull() ?: 0
                            else -> 0
                        }
                        val pontosTotais = when (val pontos = playerMap["pontos_totais"]) {
                            is Number -> pontos.toInt()
                            is String -> pontos.toIntOrNull() ?: 0
                            else -> 0
                        }
                        
                        mapOf<String, Any>(
                            "id" to id,
                            "nome" to nome,
                            "jogos_jogados" to jogosJogados,
                            "pontos_totais" to pontosTotais
                        )
                    } else null
                } ?: emptyList()
                
                Log.d(TAG, "✅ Ranking geral carregado com sucesso: ${ranking.size} jogadores")
                ranking
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao carregar ranking geral via HTTP: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getFinishedGames(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "=== CARREGANDO JOGOS FINALIZADOS VIA HTTP ===")
            Log.d(TAG, "URL: $BASE_URL/finished-games")
            
            val url = URL("$BASE_URL/finished-games")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            Log.d(TAG, "Fazendo conexão HTTP...")
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Lendo resposta...")
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta completa: $response")
                
                val jsonResponse = gson.fromJson(response, Map::class.java)
                val gamesData = jsonResponse["games"] as? List<*>
                
                val games = gamesData?.mapNotNull { gameData ->
                    val gameMap = gameData as? Map<*, *>
                    if (gameMap != null) {
                        val id = (gameMap["id"] as? Number)?.toInt() ?: 0
                        val codigoJogo = (gameMap["codigo_jogo"] as? String) ?: ""
                        val finalizado = (gameMap["finalizado"] as? Boolean) ?: false
                        val totalRodadas = when (val rodadas = gameMap["total_rodadas"]) {
                            is Number -> rodadas.toInt()
                            is String -> rodadas.toIntOrNull() ?: 0
                            else -> 0
                        }
                        val totalJogadores = when (val jogadores = gameMap["total_jogadores"]) {
                            is Number -> jogadores.toInt()
                            is String -> jogadores.toIntOrNull() ?: 0
                            else -> 0
                        }
                        
                        mapOf<String, Any>(
                            "id" to id,
                            "codigo_jogo" to codigoJogo,
                            "finalizado" to finalizado,
                            "total_rodadas" to totalRodadas,
                            "total_jogadores" to totalJogadores
                        )
                    } else null
                } ?: emptyList()
                
                Log.d(TAG, "✅ Jogos finalizados carregados com sucesso: ${games.size} jogos")
                games
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao carregar jogos finalizados via HTTP: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun insertPontuacao(pontuacao: PontuacaoJogoEntity): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Inserindo pontuação via HTTP: Jogo ${pontuacao.idJogo} - Jogador ${pontuacao.idJogador}")
            
            val url = URL("$BASE_URL/pontuacoes")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val jsonData = gson.toJson(mapOf(
                "id_jogo" to pontuacao.idJogo,
                "id_jogador" to pontuacao.idJogador,
                "pontuacao" to pontuacao.pontuacao,
                "rodada" to pontuacao.rodada
            ))
            
            Log.d(TAG, "Enviando dados da pontuação: $jsonData")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonData)
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta: $response")
                Log.d(TAG, "✅ Pontuação inserida com sucesso via HTTP")
                true
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inserir pontuação via HTTP: ${e.message}", e)
            false
        }
    }

    suspend fun getPlayerByName(playerName: String): PlayerEntity? = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Buscando jogador por nome via HTTP: $playerName")
            
            val url = URL("$BASE_URL/players/$playerName")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta: $response")
                
                val jsonResponse = gson.fromJson(response, Map::class.java)
                val playerData = jsonResponse["player"]
                
                if (playerData != null) {
                    val playerMap = playerData as Map<*, *>
                    val player = PlayerEntity(
                        id = (playerMap["id"] as? Number)?.toInt() ?: 0,
                        nome = playerMap["nome"] as String,
                        dataCriacao = Date(), // Simplificado
                        dataAtualizacao = Date() // Simplificado
                    )
                    Log.d(TAG, "✅ Jogador encontrado via HTTP: ${player.nome}")
                    player
                } else {
                    Log.d(TAG, "✅ Jogador não encontrado via HTTP")
                    null
                }
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar jogador via HTTP: ${e.message}", e)
            null
        }
    }

    suspend fun getMatchesByGameId(gameId: String): List<MatchEntity> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Carregando partidas via HTTP")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao carregar partidas via HTTP: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getPontuacoesByJogo(gameId: String): List<PontuacaoJogoEntity> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Carregando pontuações via HTTP")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao carregar pontuações via HTTP: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun updatePlayerScore(playerId: String, newScore: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Atualizando pontuação via HTTP: $playerId -> $newScore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao atualizar pontuação via HTTP: ${e.message}", e)
            false
        }
    }

    suspend fun initializeDatabase(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Inicializando banco via HTTP")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar banco via HTTP: ${e.message}", e)
            false
        }
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "=== TESTANDO CONECTIVIDADE ===")
            Log.d(TAG, "URL: $BASE_URL/test")
            Log.d(TAG, "Criando URL...")
            
            val url = URL("$BASE_URL/test")
            Log.d(TAG, "URL criada: $url")
            
            Log.d(TAG, "Abrindo conexão...")
            val connection = url.openConnection() as HttpURLConnection
            Log.d(TAG, "Conexão aberta")
            
            Log.d(TAG, "Configurando método GET...")
            connection.requestMethod = "GET"
            Log.d(TAG, "Método configurado")
            
            Log.d(TAG, "Configurando timeout...")
            connection.connectTimeout = 5000 // 5 segundos
            connection.readTimeout = 5000 // 5 segundos
            Log.d(TAG, "Timeout configurado")
            
            Log.d(TAG, "Fazendo teste de conexão...")
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Lendo resposta...")
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta do teste: $response")
                Log.d(TAG, "✅ Conectividade OK!")
                true
            } else {
                Log.e(TAG, "❌ Erro na conectividade: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro no teste de conectividade: ${e.message}", e)
            Log.e(TAG, "Stack trace completo:", e)
            false
        }
    }

    suspend fun getGameDetails(gameId: Int): Map<String, Any>? = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "=== CARREGANDO DETALHES DO JOGO $gameId VIA HTTP ===")
            Log.d(TAG, "URL: $BASE_URL/game-details/$gameId")
            
            val url = URL("$BASE_URL/game-details/$gameId")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            Log.d(TAG, "Fazendo conexão HTTP...")
            val responseCode = connection.responseCode
            Log.d(TAG, "Código de resposta: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Lendo resposta...")
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                Log.d(TAG, "Resposta completa: $response")
                
                val jsonResponse = gson.fromJson(response, Map::class.java)
                val data = jsonResponse["data"] as? Map<*, *>
                
                if (data != null) {
                    val game = data["game"] as? Map<*, *>
                    val matches = data["matches"] as? List<*>
                    val players = data["players"] as? List<*>
                    
                    Log.d(TAG, "Game: $game")
                    Log.d(TAG, "Matches size: ${matches?.size}")
                    Log.d(TAG, "Players size: ${players?.size}")
                    
                    val result = mapOf<String, Any>(
                        "game" to (game?.let { 
                            it.entries.associate { (k, v) -> k.toString() to v }
                        } ?: emptyMap<String, Any>()),
                        "matches" to (matches?.mapNotNull { match ->
                            when (match) {
                                is Map<*, *> -> match.entries.associate { (k, v) -> k.toString() to v }
                                else -> null
                            }
                        } ?: emptyList<Map<String, Any>>()),
                        "players" to (players?.mapNotNull { player ->
                            when (player) {
                                is Map<*, *> -> player.entries.associate { (k, v) -> k.toString() to v }
                                else -> null
                            }
                        } ?: emptyList<Map<String, Any>>())
                    )
                    
                    Log.d(TAG, "Result final: $result")
                    result
                } else {
                    Log.e(TAG, "❌ Dados do jogo não encontrados")
                    null
                }
            } else {
                Log.e(TAG, "❌ Erro HTTP: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao carregar detalhes do jogo via HTTP: ${e.message}", e)
            null
        }
    }
} 