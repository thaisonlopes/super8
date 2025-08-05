package com.beach.super8.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.beach.super8.model.GameMatch
import com.beach.super8.model.Round
import com.beach.super8.ui.theme.*
import com.beach.super8.viewmodel.PostgresGameViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable

@Composable
fun GameHistoryScreen(
    viewModel: PostgresGameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGameDetails: (String) -> Unit = {}
) {
    val finishedGames by viewModel.finishedGames.collectAsState()
    val currentGame by viewModel.currentGame.collectAsState()

    // Carregar histórico quando a tela for iniciada
    LaunchedEffect(Unit) {
        viewModel.loadFinishedGames()
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
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
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Histórico de Torneios",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Veja todos os torneios já realizados",
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Se há jogo atual finalizado, mostrar primeiro
            currentGame?.let { game ->
                if (game.isFinished) {
                    AnimatedVisibility(visible = true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFe8f5e9)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Torneio Atual Finalizado",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Código: ${game.gameCode}",
                                    fontSize = 15.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    InfoItem("Rodadas", "${game.rounds.size}/7", Icons.Default.Refresh, OceanBlue)
                                    InfoItem("Jogadores", "${game.players.size}", Icons.Default.People, PrimaryGreen)
                                    InfoItem("Status", "Editável", Icons.Default.Edit, PrimaryGreen)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Histórico de torneios
            if (finishedGames.isNotEmpty()) {
                Text(
                    text = "Torneios Finalizados (${finishedGames.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlue,
                    modifier = Modifier.padding(start = 22.dp, top = 12.dp, bottom = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(finishedGames) { game ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .clickable {
                                    // Carregar detalhes do jogo e navegar
                                    val gameId = game.id.toIntOrNull()
                                    if (gameId != null) {
                                        viewModel.loadGameDetails(gameId)
                                        onNavigateToGameDetails(game.gameCode)
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Torneio ${game.gameCode}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Carregar dados reais do jogo
                                val gameId = game.id.toIntOrNull()
                                var gameDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
                                
                                LaunchedEffect(gameId) {
                                    if (gameId != null) {
                                        gameDetails = viewModel.getGameDetailsForUI(gameId)
                                    }
                                }
                                
                                val players = gameDetails?.get("players") as? List<Map<*, *>> ?: emptyList()
                                val matches = gameDetails?.get("matches") as? List<Map<*, *>> ?: emptyList()
                                
                                val totalPlayers = players.size
                                val totalRounds = matches.mapNotNull { 
                                    when (val rodada = it["rodada"]) {
                                        is Number -> rodada.toInt()
                                        is String -> rodada.toIntOrNull()
                                        else -> null
                                    }
                                }.distinct().size
                                val totalGames = matches.size
                                
                                Text(
                                    text = "$totalPlayers jogadores • $totalRounds rodadas • $totalGames jogos",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Vencedores
                                if (players.isNotEmpty()) {
                                    val sortedPlayers = players.sortedByDescending { 
                                        when (val pontos = it["pontos_totais"]) {
                                            is Number -> pontos.toInt()
                                            is String -> pontos.toIntOrNull() ?: 0
                                            else -> 0
                                        }
                                    }
                                    
                                    if (sortedPlayers.isNotEmpty()) {
                                        val winner = sortedPlayers[0]
                                        val winnerName = winner["nome"] as? String ?: ""
                                        val winnerPoints = when (val pontos = winner["pontos_totais"]) {
                                            is Number -> pontos.toInt()
                                            is String -> pontos.toIntOrNull() ?: 0
                                            else -> 0
                                        }
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "🏆 ",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Gold
                                            )
                                            Text(
                                                text = winnerName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Gold
                                            )
                                            Text(
                                                text = " ($winnerPoints pts)",
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )
                                            
                                            if (sortedPlayers.size > 1) {
                                                val runnerUp = sortedPlayers[1]
                                                val runnerUpName = runnerUp["nome"] as? String ?: ""
                                                val runnerUpPoints = when (val pontos = runnerUp["pontos_totais"]) {
                                                    is Number -> pontos.toInt()
                                                    is String -> pontos.toIntOrNull() ?: 0
                                                    else -> 0
                                                }
                                                
                                                Text(
                                                    text = "  •  🥈 $runnerUpName ($runnerUpPoints pts)",
                                                    fontSize = 13.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Lista vazia
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum torneio finalizado ainda",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Os torneios aparecerão aqui após serem finalizados",
                            fontSize = 14.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Botão voltar
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voltar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
} 