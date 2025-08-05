package com.beach.super8.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beach.super8.model.Game
import com.beach.super8.model.GameMatch
import com.beach.super8.model.Round
import com.beach.super8.ui.theme.*
import com.beach.super8.viewmodel.PostgresGameViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.animateContentSize

@Composable
fun GameSpecificHistoryScreen(
    gameCode: String,
    viewModel: PostgresGameViewModel,
    onNavigateBack: () -> Unit
) {
    val currentGame by viewModel.currentGame.collectAsState()
    val finishedGames by viewModel.finishedGames.collectAsState()
    
    // Carregar detalhes do jogo quando a tela for iniciada
    LaunchedEffect(gameCode) {
        // Não precisamos mais chamar loadGameDetails aqui
        Log.d("GameSpecificHistoryScreen", "Tela iniciada para gameCode: $gameCode")
    }

    // Buscar o jogo pelo código
    val game = currentGame?.takeIf { it.gameCode == gameCode }
        ?: finishedGames.find { it.gameCode == gameCode }

    Log.d("GameSpecificHistoryScreen", "=== BUSCANDO JOGO ===")
    Log.d("GameSpecificHistoryScreen", "GameCode procurado: $gameCode")
    Log.d("GameSpecificHistoryScreen", "CurrentGame: ${currentGame?.gameCode}")
    Log.d("GameSpecificHistoryScreen", "FinishedGames: ${finishedGames.map { it.gameCode }}")
    Log.d("GameSpecificHistoryScreen", "Jogo encontrado: ${game?.gameCode} (ID: ${game?.id})")

    if (game == null) {
        Log.e("GameSpecificHistoryScreen", "JOGO NÃO ENCONTRADO!")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Torneio não encontrado", color = Color.Red, fontSize = 20.sp)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFe0f7fa), // azul claro
                        Color(0xFFf8fafc), // quase branco
                        Color.White
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header com botão voltar
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = OceanBlue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Voltar", 
                            tint = Color.White, 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Rodadas do Torneio ${game.gameCode}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Resultados e estatísticas",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Carregar dados reais do jogo
            val gameId = when (val id = game.id) {
                is String -> id.toIntOrNull()
                is Int -> id
                is Number -> id.toInt()
                else -> null
            }
            var gameDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
            
            Log.d("GameSpecificHistoryScreen", "=== INICIANDO CARREGAMENTO ===")
            Log.d("GameSpecificHistoryScreen", "Game ID original: ${game.id}")
            Log.d("GameSpecificHistoryScreen", "Game ID parsed: $gameId")
            Log.d("GameSpecificHistoryScreen", "Game: $game")
            Log.d("GameSpecificHistoryScreen", "GameCode: $gameCode")
            
            LaunchedEffect(gameId) {
                if (gameId != null) {
                    try {
                        Log.d("GameSpecificHistoryScreen", "Carregando detalhes do jogo $gameId")
                        gameDetails = viewModel.getGameDetailsForUI(gameId)
                        Log.d("GameSpecificHistoryScreen", "Detalhes carregados: $gameDetails")
                        Log.d("GameSpecificHistoryScreen", "GameDetails keys: ${gameDetails?.keys}")
                    } catch (e: Exception) {
                        Log.e("GameSpecificHistoryScreen", "Erro ao carregar detalhes: ${e.message}", e)
                    }
                } else {
                    Log.e("GameSpecificHistoryScreen", "GameId é null!")
                }
            }
            
            val players = when (val playersData = gameDetails?.get("players")) {
                is List<*> -> {
                    Log.d("GameSpecificHistoryScreen", "PlayersData type: ${playersData.javaClass}")
                    Log.d("GameSpecificHistoryScreen", "PlayersData first item: ${playersData.firstOrNull()}")
                    playersData.mapNotNull { 
                        when (it) {
                            is Map<*, *> -> it.entries.associate { (k, v) -> k.toString() to v }
                            else -> {
                                Log.d("GameSpecificHistoryScreen", "Unknown type: ${it?.javaClass}")
                                null
                            }
                        }
                    }
                }
                else -> {
                    Log.d("GameSpecificHistoryScreen", "PlayersData is not List: ${playersData?.javaClass}")
                    emptyList()
                }
            }
            val matches = when (val matchesData = gameDetails?.get("matches")) {
                is List<*> -> {
                    Log.d("GameSpecificHistoryScreen", "MatchesData type: ${matchesData.javaClass}")
                    Log.d("GameSpecificHistoryScreen", "MatchesData first item: ${matchesData.firstOrNull()}")
                    matchesData.mapNotNull { 
                        when (it) {
                            is Map<*, *> -> it.entries.associate { (k, v) -> k.toString() to v }
                            else -> {
                                Log.d("GameSpecificHistoryScreen", "Unknown type: ${it?.javaClass}")
                                null
                            }
                        }
                    }
                }
                else -> {
                    Log.d("GameSpecificHistoryScreen", "MatchesData is not List: ${matchesData?.javaClass}")
                    emptyList()
                }
            }
            
            Log.d("GameSpecificHistoryScreen", "Players size: ${players.size}")
            Log.d("GameSpecificHistoryScreen", "Matches size: ${matches.size}")
            Log.d("GameSpecificHistoryScreen", "GameDetails keys: ${gameDetails?.keys}")
            Log.d("GameSpecificHistoryScreen", "GameDetails: $gameDetails")
            
            // Estatísticas do torneio
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "📊 Estatísticas do Torneio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val totalPlayers = players.size
                    val totalRounds = matches.mapNotNull { 
                        when (val rodada = it["rodada"]) {
                            is Number -> rodada.toInt()
                            is String -> rodada.toIntOrNull()
                            else -> null
                        }
                    }.distinct().size
                    val totalGames = matches.size
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatisticItem("Jogadores", totalPlayers.toString(), Icons.Default.People, PrimaryGreen)
                        StatisticItem("Rodadas", totalRounds.toString(), Icons.Default.Refresh, OceanBlue)
                        StatisticItem("Jogos", totalGames.toString(), Icons.Default.SportsTennis, Gold)
                    }
                    
                    // Vencedores
                    if (players.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🏆 Pódio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val sortedPlayers = players.sortedByDescending { 
                            when (val pontos = it["pontos_totais"]) {
                                is Number -> pontos.toInt()
                                is String -> pontos.toIntOrNull() ?: 0
                                else -> 0
                            }
                        }
                        
                        sortedPlayers.take(3).forEachIndexed { index, player ->
                            val playerName = player["nome"] as? String ?: ""
                            val playerPoints = when (val pontos = player["pontos_totais"]) {
                                is Number -> pontos.toInt()
                                is String -> pontos.toIntOrNull() ?: 0
                                else -> 0
                            }
                            
                            val medal = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> ""
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$medal $playerName",
                                    fontSize = 15.sp,
                                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (index == 0) Gold else Color.Black
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "$playerPoints pts",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Lista de rodadas
            Text(
                text = "📋 Resultados por Rodada",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OceanBlue,
                modifier = Modifier.padding(start = 22.dp, top = 12.dp, bottom = 4.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Agrupar partidas por rodada
                val matchesByRound = matches.groupBy { 
                    when (val rodada = it["rodada"]) {
                        is Number -> rodada.toInt()
                        is String -> rodada.toIntOrNull() ?: 0
                        else -> 0
                    }
                }
                val sortedRounds = matchesByRound.keys.sorted()
                
                items(sortedRounds.size) { index ->
                    val roundNumber = sortedRounds[index]
                    val roundMatches = matchesByRound[roundNumber] ?: emptyList()
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Rodada $roundNumber",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            roundMatches.forEach { match ->
                                val jogador1Nome = match["jogador1_nome"] as? String ?: ""
                                val jogador2Nome = match["jogador2_nome"] as? String ?: ""
                                val pontuacao1 = when (val p1 = match["pontuacao_jogador1"]) {
                                    is Number -> p1.toInt()
                                    is String -> p1.toIntOrNull() ?: 0
                                    else -> 0
                                }
                                val pontuacao2 = when (val p2 = match["pontuacao_jogador2"]) {
                                    is Number -> p2.toInt()
                                    is String -> p2.toIntOrNull() ?: 0
                                    else -> 0
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$jogador1Nome vs $jogador2Nome",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$pontuacao1 x $pontuacao2",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
} 