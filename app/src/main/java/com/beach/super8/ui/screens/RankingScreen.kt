package com.beach.super8.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.beach.super8.ui.theme.*
import com.beach.super8.viewmodel.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RankingScreen(
    viewModel: GameViewModel,
    onNavigateToHistory: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    Log.d("RankingScreen", "=== RANKING SCREEN INICIADA ===")
    
    val currentGame by viewModel.currentGame.collectAsState()
    var isDataProcessed by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<com.beach.super8.model.Player?>(null) }
    var editedScore by remember { mutableStateOf("") }
    
    Log.d("RankingScreen", "CurrentGame: ${currentGame?.gameCode}")
    Log.d("RankingScreen", "CurrentGame isFinished: ${currentGame?.isFinished}")
    Log.d("RankingScreen", "isDataProcessed: $isDataProcessed")
    
    // Função para processar dados
    val processData: () -> Unit = {
        try {
            Log.d("RankingScreen", "=== PROCESSANDO DADOS ===")
            currentGame?.let { game ->
                viewModel.saveGameToHistory(game)
                Log.d("RankingScreen", "Jogo salvo no histórico: ${game.gameCode}")
            }
            isDataProcessed = true
            Log.d("RankingScreen", "Dados processados com sucesso!")
        } catch (e: Exception) {
            Log.e("RankingScreen", "ERRO AO PROCESSAR DADOS: ${e.message}", e)
        }
    }
    
    // Função para navegar para histórico específico
    val navigateToSpecificHistory: () -> Unit = {
        try {
            Log.d("RankingScreen", "=== NAVEGANDO PARA HISTÓRICO ESPECÍFICO ===")
            currentGame?.let { game ->
                onNavigateToHistory(game.gameCode)
            }
        } catch (e: Exception) {
            Log.e("RankingScreen", "ERRO AO NAVEGAR: ${e.message}", e)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E8),
                        Color(0xFFF0F8F0),
                        Color.White
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                // Header com troféu
                HeaderSection()
            }
            
            item {
                // Informações do torneio
                currentGame?.let { game ->
                    TournamentInfoCard(game = game)
                }
            }
            
            item {
                // Vencedores destacados (2 vencedores)
                currentGame?.let { game ->
                    val sortedPlayers = game.players.sortedByDescending { it.totalPoints }
                    val winners = sortedPlayers.take(2)
                    if (winners.size >= 2) {
                        WinnersCard(winners = winners)
                    }
                }
            }
            
            item {
                // Título do ranking
                RankingTitleSection()
            }
            
            // Lista de jogadores ordenados por pontos
            currentGame?.let { game ->
                val sortedPlayers = game.players.sortedByDescending { it.totalPoints }
                
                // Verificar se há empate entre os 2 primeiros
                val hasTie = sortedPlayers.size >= 2 && sortedPlayers[0].totalPoints == sortedPlayers[1].totalPoints
                
                itemsIndexed(sortedPlayers) { index, player ->
                    PlayerRankingCard(
                        position = index + 1,
                        player = player,
                        isWinner = index < 2,
                        isLast = index >= sortedPlayers.size - 2,
                        isDataProcessed = isDataProcessed,
                        showEditButton = hasTie && index < 2 && !isDataProcessed, // Só mostra lápis se há empate e não processado
                        onEditScore = { 
                            if (!isDataProcessed && hasTie && index < 2) {
                                selectedPlayer = player
                                editedScore = player.totalPoints.toString()
                                showEditDialog = true
                            }
                        }
                    )
                }
            }
            
            item {
                // Estatísticas do torneio
                currentGame?.let { game ->
                    TournamentStatsCard(game = game)
                }
            }
            
            item {
                // Botões de ação
                RankingActionButtonsSection(
                    isDataProcessed = isDataProcessed,
                    isProcessing = false,
                    onProcessData = processData,
                    onNavigateToHistory = navigateToSpecificHistory,
                    onNavigateToHome = onNavigateToHome
                )
            }
        }
    }
    
    // Dialog para editar pontuação
    if (showEditDialog && selectedPlayer != null) {
        EditScoreDialog(
            player = selectedPlayer!!,
            currentScore = editedScore,
            onScoreChanged = { newScore ->
                editedScore = newScore
            },
            onConfirm = {
                val newScore = editedScore.toIntOrNull() ?: selectedPlayer!!.totalPoints
                viewModel.updatePlayerScore(selectedPlayer!!.id, newScore)
                showEditDialog = false
                selectedPlayer = null
                editedScore = ""
            },
            onDismiss = {
                showEditDialog = false
                selectedPlayer = null
                editedScore = ""
            }
        )
    }
    
    Log.d("RankingScreen", "=== RANKING SCREEN NOVA RENDERIZADA ===")
}

@Composable
fun GeneralRankingScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    Log.d("GeneralRankingScreen", "=== RANKING GERAL INICIADO ===")
    
    val savedPlayers by viewModel.savedPlayers.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E8),
                        Color(0xFFF0F8F0),
                        Color.White
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                // Header
                GeneralRankingHeader(onNavigateBack = onNavigateBack)
            }
            
            item {
                // Título do ranking geral
                GeneralRankingTitleSection()
            }
            
            // Lista de jogadores ordenados por pontos totais
            val sortedPlayers = savedPlayers.sortedByDescending { it.totalPoints }
            itemsIndexed(sortedPlayers) { index, player ->
                GeneralPlayerRankingCard(
                    position = index + 1,
                    player = player
                )
            }
            
            item {
                // Estatísticas gerais
                GeneralStatsCard(players = savedPlayers)
            }
        }
    }
    
    Log.d("GeneralRankingScreen", "=== RANKING GERAL RENDERIZADO ===")
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Gold,
                        Color(0xFFFFD700)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Troféu grande
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Torneio Finalizado!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Parabéns a todos os participantes!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GeneralRankingHeader(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PrimaryGreen,
                        DarkGreen
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Ranking Geral",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "Soma de todos os pontos",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun TournamentInfoCard(game: com.beach.super8.model.Game) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OceanBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Informações do Torneio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Código: ${game.gameCode}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Text(
                    text = "7 rodadas • 14 jogos • ${game.players.size} jogadores",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun WinnersCard(winners: List<com.beach.super8.model.Player>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Coroa dos vencedores
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Gold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "🏆 CAMPEÕES 🏆",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mostrar os 2 vencedores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                winners.forEachIndexed { index, winner ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${index + 1}º Lugar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = winner.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${winner.totalPoints} pontos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryGreen,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Verificar se há empate
            if (winners.size >= 2 && winners[0].totalPoints == winners[1].totalPoints) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Empate! Use o lápis para desempatar",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingTitleSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Leaderboard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Ranking Final",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GeneralRankingTitleSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Leaderboard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Ranking Geral - Todos os Torneios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PlayerRankingCard(
    position: Int,
    player: com.beach.super8.model.Player,
    isWinner: Boolean,
    isLast: Boolean,
    isDataProcessed: Boolean,
    showEditButton: Boolean,
    onEditScore: () -> Unit
) {
    val backgroundColor = when {
        isLast && !isDataProcessed -> Color(0xFFFFEBEE) // Vermelho claro para os últimos
        position == 1 -> Color(0xFFFFF8E1) // Dourado claro
        position == 2 -> Color(0xFFF5F5F5) // Prata claro
        position == 3 -> Color(0xFFF3E5F5) // Bronze claro
        else -> Color.White
    }
    
    val textColor = if (isLast && !isDataProcessed) Color.Red else Color.Black
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posição
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isLast && !isDataProcessed -> Color.Red
                            position == 1 -> Gold
                            position == 2 -> Color.Gray
                            position == 3 -> Color(0xFF8D6E63)
                            else -> PrimaryGreen
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = position.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Nome do jogador
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = player.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                
                if (isWinner) {
                    Text(
                        text = "🏆 Campeão",
                        fontSize = 12.sp,
                        color = Gold,
                        fontWeight = FontWeight.Medium
                    )
                } else if (isLast && !isDataProcessed) {
                    Text(
                        text = "⚠️ Últimos",
                        fontSize = 12.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Pontos
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${player.totalPoints}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLast && !isDataProcessed) Color.Red else PrimaryGreen
                )
                
                Text(
                    text = "pontos",
                    fontSize = 12.sp,
                    color = if (isLast && !isDataProcessed) Color.Red else Color.Gray
                )
            }
            
            // Lápis de edição (apenas para os 2 primeiros e se não processado)
            if (showEditButton) {
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onEditScore,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFF3E0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar pontuação",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralPlayerRankingCard(
    position: Int,
    player: com.beach.super8.model.Player
) {
    val backgroundColor = when (position) {
        1 -> Color(0xFFFFF8E1) // Dourado claro
        2 -> Color(0xFFF5F5F5) // Prata claro
        3 -> Color(0xFFF3E5F5) // Bronze claro
        else -> Color.White
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posição
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (position) {
                            1 -> Gold
                            2 -> Color.Gray
                            3 -> Color(0xFF8D6E63)
                            else -> PrimaryGreen
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = position.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Nome do jogador
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = player.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                if (position <= 3) {
                    Text(
                        text = when (position) {
                            1 -> "🥇 Líder Geral"
                            2 -> "🥈 Vice-Líder"
                            3 -> "🥉 Terceiro Lugar"
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = Gold,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "${player.gamesPlayed} torneios jogados",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            // Pontos totais
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${player.totalPoints}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                
                Text(
                    text = "pontos totais",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun TournamentStatsCard(game: com.beach.super8.model.Game) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OceanBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Estatísticas do Torneio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Jogadores",
                    value = "${game.players.size}",
                    icon = Icons.Default.People,
                    color = PrimaryGreen
                )
                
                StatItem(
                    label = "Rodadas",
                    value = "7",
                    icon = Icons.Default.Refresh,
                    color = OceanBlue
                )
                
                StatItem(
                    label = "Jogos",
                    value = "14",
                    icon = Icons.Default.SportsTennis,
                    color = Gold
                )
            }
        }
    }
}

@Composable
private fun GeneralStatsCard(players: List<com.beach.super8.model.Player>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OceanBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Estatísticas Gerais",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Jogadores",
                    value = "${players.size}",
                    icon = Icons.Default.People,
                    color = PrimaryGreen
                )
                
                StatItem(
                    label = "Torneios",
                    value = "${players.sumOf { it.gamesPlayed }}",
                    icon = Icons.Default.EmojiEvents,
                    color = OceanBlue
                )
                
                StatItem(
                    label = "Total Pontos",
                    value = "${players.sumOf { it.totalPoints }}",
                    icon = Icons.Default.Star,
                    color = Gold
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.1f)),
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

@Composable
private fun RankingActionButtonsSection(
    isDataProcessed: Boolean,
    isProcessing: Boolean,
    onProcessData: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botão Processar Dados (só aparece se não processado)
        if (!isDataProcessed) {
            Button(
                onClick = onProcessData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp),
                enabled = !isProcessing
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isProcessing) "Processando..." else "Processar Dados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
        
        Button(
            onClick = {
                Log.d("RankingScreen", "=== BOTÃO HISTORY CLICADO ===")
                onNavigateToHistory()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver Histórico",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Botão Volte ao Início (só habilitado após processar)
        Log.d("RankingScreen", "=== RENDERIZANDO BOTÃO VOLTE AO INÍCIO ===")
        Log.d("RankingScreen", "isDataProcessed: $isDataProcessed")
        Log.d("RankingScreen", "Botão habilitado: $isDataProcessed")
        
        Button(
            onClick = {
                Log.d("RankingScreen", "=== BOTÃO VOLTE AO INÍCIO CLICADO ===")
                Log.d("RankingScreen", "isDataProcessed: $isDataProcessed")
                Log.d("RankingScreen", "onNavigateToHome function: ${onNavigateToHome != null}")
                
                if (isDataProcessed) {
                    try {
                        Log.d("RankingScreen", "Executando onNavigateToHome...")
                        onNavigateToHome()
                        Log.d("RankingScreen", "onNavigateToHome executado com sucesso!")
                    } catch (e: Exception) {
                        Log.e("RankingScreen", "ERRO AO NAVEGAR: ${e.message}", e)
                        Log.e("RankingScreen", "Stack trace completo:", e)
                    }
                } else {
                    Log.d("RankingScreen", "Dados não processados, navegação bloqueada")
                    Log.d("RankingScreen", "isDataProcessed: $isDataProcessed")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDataProcessed) OceanBlue else Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = isDataProcessed
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDataProcessed) "Volte ao Início" else "Processe os dados primeiro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 

@Composable
private fun EditScoreDialog(
    player: com.beach.super8.model.Player,
    currentScore: String,
    onScoreChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Editar Pontuação",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Jogador: ${player.name}",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = currentScore,
                    onValueChange = { newValue ->
                        val filteredValue = newValue.filter { char -> char.isDigit() }
                        onScoreChanged(filteredValue)
                    },
                    placeholder = {
                        Text(
                            text = "Nova pontuação",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFFFF9800),
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = currentScore.isNotBlank()
                    ) {
                        Text(
                            text = "Confirmar",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
} 