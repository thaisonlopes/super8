package com.beach.super8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beach.super8.R
import com.beach.super8.ui.theme.*
import com.beach.super8.viewmodel.GameViewModel
import androidx.compose.ui.text.style.TextAlign

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val currentGame by viewModel.currentGame.collectAsState()
    val currentRound by viewModel.currentRound.collectAsState()
    
    // Log para verificar pontos em tempo real
    LaunchedEffect(currentGame) {
        currentGame?.let { game ->
            android.util.Log.d("GamePlayScreen", "=== PONTOS ATUALIZADOS ===")
            android.util.Log.d("GamePlayScreen", "Jogadores e pontos: ${game.players.map { "${it.name}: ${it.totalPoints} pontos" }}")
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
                // Header
                HeaderSection()
            }
            
            item {
                // Game Info Card
                currentGame?.let { game ->
                    GameInfoCard(game = game)
                }
            }
            
            item {
                // Current Round Info with Score Inputs
                currentRound?.let { round ->
                    CurrentRoundWithScoresCard(
                        round = round,
                        onScoreUpdate = { gameIndex, pair1Score, pair2Score ->
                            android.util.Log.d("GamePlayScreen", "=== CHAMANDO UPDATE ROUND SCORE ===")
                            android.util.Log.d("GamePlayScreen", "GameIndex: $gameIndex, Scores: $pair1Score x $pair2Score")
                            viewModel.updateRoundScore(gameIndex, pair1Score, pair2Score)
                        }
                    )
                }
            }
            
            item {
                // Navigation Buttons
                NavigationButtonsSection(
                    currentGame = currentGame,
                    currentRound = currentRound,
                    onPreviousRound = { viewModel.previousRound() },
                    onNextRound = { viewModel.nextRound() },
                    onFinishTournament = {
                        android.util.Log.d("GamePlayScreen", "=== FINALIZANDO TORNEIO ===")
                        android.util.Log.d("GamePlayScreen", "Pontos finais: ${currentGame?.players?.map { "${it.name}: ${it.totalPoints} pontos" }}")
                        viewModel.finishTournament()
                        onNavigateToRanking()
                    },
                    onNavigateBack = onNavigateBack,
                    onNavigateToHome = onNavigateToHome
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsTennis,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Jogo em Andamento",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "Inserir placares das duplas",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun GameInfoCard(game: com.beach.super8.model.Game) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
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
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Informações do Jogo",
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
                InfoItem(
                    label = "Código",
                    value = game.gameCode,
                    icon = Icons.Default.Tag,
                    color = PrimaryGreen
                )
                InfoItem(
                    label = "Rodada",
                    value = "${game.currentRound}/7",
                    icon = Icons.Default.Refresh,
                    color = OceanBlue
                )
                InfoItem(
                    label = "Jogadores",
                    value = "${game.players.size}",
                    icon = Icons.Default.People,
                    color = Gold
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
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
private fun CurrentRoundWithScoresCard(
    round: com.beach.super8.model.Round,
    onScoreUpdate: (Int, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
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
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsTennis,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Rodada ${round.roundNumber}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Jogo 1 com placar
            GameWithScoreSection(
                gameNumber = 1,
                game = round.game1,
                onScoreUpdate = { pair1Score, pair2Score ->
                    onScoreUpdate(0, pair1Score, pair2Score)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Jogo 2 com placar
            GameWithScoreSection(
                gameNumber = 2,
                game = round.game2,
                onScoreUpdate = { pair1Score, pair2Score ->
                    onScoreUpdate(1, pair1Score, pair2Score)
                }
            )
        }
    }
}

@Composable
private fun GameWithScoreSection(
    gameNumber: Int,
    game: com.beach.super8.model.GameMatch,
    onScoreUpdate: (Int, Int) -> Unit
) {
    // Usar key única que muda quando o jogo muda
    val gameKey = "${game.pair1.first.id}_${game.pair1.second.id}_${game.pair2.first.id}_${game.pair2.second.id}"
    
    var pair1Score by remember(gameKey) { mutableStateOf("") }
    var pair2Score by remember(gameKey) { mutableStateOf("") }
    
    // Resetar campos quando o jogo não está completo
    LaunchedEffect(game.isCompleted) {
        if (!game.isCompleted) {
            pair1Score = ""
            pair2Score = ""
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (gameNumber == 1) LightGreen.copy(alpha = 0.1f) else OceanBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dupla A
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dupla A",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "${game.pair1.first.name} & ${game.pair1.second.name}",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = pair1Score,
                        onValueChange = { newValue -> 
                            val filteredValue = newValue.filter { char -> char.isDigit() }
                            val score = filteredValue.toIntOrNull() ?: 0
                            
                            // Validar se não passa de 6
                            if (score <= 6) {
                                pair1Score = filteredValue
                                val score1 = score
                                val score2 = pair2Score.toIntOrNull() ?: 0
                                if (score1 + score2 <= 6) {
                                    android.util.Log.d("GamePlayScreen", "=== ENVIANDO PLACAR JOGO $gameNumber ===")
                                    android.util.Log.d("GamePlayScreen", "Dupla A: $score1, Dupla B: $score2")
                                    android.util.Log.d("GamePlayScreen", "Jogadores: ${game.pair1.first.name} & ${game.pair1.second.name} vs ${game.pair2.first.name} & ${game.pair2.second.name}")
                                    onScoreUpdate(score1, score2)
                                }
                            }
                        },
                        modifier = Modifier.width(60.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
                
                // VS
                Text(
                    text = "VS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                // Dupla B
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dupla B",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = OceanBlue
                    )
                    Text(
                        text = "${game.pair2.first.name} & ${game.pair2.second.name}",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = pair2Score,
                        onValueChange = { newValue -> 
                            val filteredValue = newValue.filter { char -> char.isDigit() }
                            val score = filteredValue.toIntOrNull() ?: 0
                            
                            // Validar se não passa de 6
                            if (score <= 6) {
                                pair2Score = filteredValue
                                val score1 = pair1Score.toIntOrNull() ?: 0
                                val score2 = score
                                if (score1 + score2 <= 6) {
                                    android.util.Log.d("GamePlayScreen", "=== ENVIANDO PLACAR JOGO $gameNumber ===")
                                    android.util.Log.d("GamePlayScreen", "Dupla A: $score1, Dupla B: $score2")
                                    android.util.Log.d("GamePlayScreen", "Jogadores: ${game.pair1.first.name} & ${game.pair1.second.name} vs ${game.pair2.first.name} & ${game.pair2.second.name}")
                                    onScoreUpdate(score1, score2)
                                }
                            }
                        },
                        modifier = Modifier.width(60.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanBlue,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
            
            // Status do jogo
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (game.isCompleted) Color(0xFFE8F5E8) else Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (game.isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (game.isCompleted) PrimaryGreen else Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = if (game.isCompleted) "Jogo Finalizado" else "Jogo Pendente",
                            fontSize = 12.sp,
                            color = if (game.isCompleted) PrimaryGreen else Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationButtonsSection(
    currentGame: com.beach.super8.model.Game?,
    currentRound: com.beach.super8.model.Round?,
    onPreviousRound: () -> Unit,
    onNextRound: () -> Unit,
    onFinishTournament: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Previous Round Button
        OutlinedButton(
            onClick = onPreviousRound,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = (currentGame?.currentRound ?: 1) > 1
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.previous_round),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Finish Current Round Button
        val isCurrentRoundCompleted = currentRound?.isCompleted == true
        val isLastRound = (currentGame?.currentRound ?: 1) >= 7 // 7 RODADAS COMPLETAS
        
        if (!isCurrentRoundCompleted) {
            // Botão para finalizar rodada atual
            Button(
                onClick = {
                    // Forçar finalização da rodada atual
                    currentRound?.let { round ->
                        if (round.game1.isCompleted && round.game2.isCompleted) {
                            // Rodada já está completa, só navegar
                            onNextRound()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp),
                enabled = currentRound?.game1?.isCompleted == true && currentRound?.game2?.isCompleted == true
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finalizar Rodada ${currentGame?.currentRound ?: 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        } else if (isLastRound) {
            // Botão para finalizar torneio
            Button(
                onClick = onFinishTournament,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finalizar Torneio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        } else {
            // Botão para próxima rodada
            Button(
                onClick = onNextRound,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ir para Rodada ${(currentGame?.currentRound ?: 1) + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Status da rodada atual
        if (!isCurrentRoundCompleted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
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
                            text = "Status da Rodada ${currentGame?.currentRound ?: 0}:",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "• Jogo 1: ${if (currentRound?.game1?.isCompleted == true) "✅ Finalizado" else "⏳ Pendente"}",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800)
                    )
                    
                    Text(
                        text = "• Jogo 2: ${if (currentRound?.game2?.isCompleted == true) "✅ Finalizado" else "⏳ Pendente"}",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
} 