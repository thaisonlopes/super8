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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beach.super8.R
import com.beach.super8.ui.theme.*
import com.beach.super8.viewmodel.PostgresGameViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: PostgresGameViewModel,
    onNavigateToGameCode: () -> Unit,
    onNavigateToGameHistory: () -> Unit,
    onNavigateToRanking: () -> Unit
) {
    Log.d("HomeScreen", "HomeScreen iniciada")
    
    val statistics by viewModel.statistics.collectAsState()
    val finishedGames by viewModel.finishedGames.collectAsState()
    val savedPlayers by viewModel.savedPlayers.collectAsState() // Adicionar dados dos jogadores
    
    // Carregar ranking geral quando a tela for exibida
    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Carregando ranking geral...")
        viewModel.reloadGeneralRanking()
        Log.d("HomeScreen", "Carregando jogos finalizados...")
        viewModel.loadFinishedGames()
    }
    
    Log.d("HomeScreen", "=== DADOS CARREGADOS ===")
    Log.d("HomeScreen", "FinishedGames size: ${finishedGames.size}")
    Log.d("HomeScreen", "FinishedGames: ${finishedGames.map { it.gameCode }}")
    Log.d("HomeScreen", "SavedPlayers size: ${savedPlayers.size}")
    Log.d("HomeScreen", "SavedPlayers: ${savedPlayers.map { "${it.name}: ${it.totalPoints} pts (${it.gamesPlayed} jogos)" }}")
    Log.d("HomeScreen", "Statistics: $statistics")
    
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
                // Header com gradiente
                HeaderSection()
            }
            
            item {
                // Menu Cards
                MenuCardsSection(
                    onNewGame = {
                        try {
                            Log.d("HomeScreen", "Botão Novo Jogo clicado")
                            onNavigateToGameCode()
                            Log.d("HomeScreen", "Navegação para GameCode executada")
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Erro ao navegar para GameCode", e)
                        }
                    },
                    onGameHistory = {
                        try {
                            Log.d("HomeScreen", "Botão Histórico clicado")
                            onNavigateToGameHistory()
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Erro ao navegar para GameHistory", e)
                        }
                    },
                    onRanking = {
                        try {
                            Log.d("HomeScreen", "Botão Ranking clicado")
                            onNavigateToRanking()
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Erro ao navegar para Ranking", e)
                        }
                    }
                )
            }
            
            item {
                // Statistics Card
                StatisticsCard(finishedGames, savedPlayers)
            }
            
            item {
                // Latest Games Card
                LatestGamesCard(finishedGames)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícone com fundo circular
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = stringResource(R.string.game_manager),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MenuCardsSection(
    onNewGame: () -> Unit,
    onGameHistory: () -> Unit,
    onRanking: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Novo Jogo - Botão que funciona
        Button(
            onClick = {
                try {
                    Log.d("HomeScreen", "=== BOTÃO NOVO JOGO CLICADO ===")
                    onNewGame()
                    Log.d("HomeScreen", "=== NAVEGAÇÃO EXECUTADA ===")
                } catch (e: Exception) {
                    Log.e("HomeScreen", "ERRO CRÍTICO: ${e.message}", e)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.new_game),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Criar um novo torneio",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Outros botões em cards menores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Histórico
            Button(
                onClick = onGameHistory,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(OceanBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Histórico",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
            
            // Ranking
            Button(
                onClick = onRanking,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ranking",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsCard(finishedGames: List<com.beach.super8.model.Game>, savedPlayers: List<com.beach.super8.model.Player>) {
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
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = stringResource(R.string.statistics),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = finishedGames.size.toString(),
                    label = stringResource(R.string.games_played),
                    icon = Icons.Default.SportsTennis,
                    color = PrimaryGreen
                )
                StatisticItem(
                    value = finishedGames.sumOf { it.rounds.size }.toString(),
                    label = stringResource(R.string.rounds_played),
                    icon = Icons.Default.Refresh,
                    color = OceanBlue
                )
                StatisticItem(
                    value = savedPlayers.size.toString(),
                    label = stringResource(R.string.unique_players),
                    icon = Icons.Default.People,
                    color = Gold
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun LatestGamesCard(finishedGames: List<com.beach.super8.model.Game>) {
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
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = stringResource(R.string.latest_games),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (finishedGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsTennis,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Nenhum jogo realizado ainda",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = "Crie seu primeiro torneio!",
                            fontSize = 14.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    finishedGames.take(3).forEach { game ->
                        GameItemCard(game)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameItemCard(game: com.beach.super8.model.Game) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Código: ${game.gameCode}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    // Data do torneio
                    Text(
                        text = "📅 ${SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "${game.players.size} jogadores • ${game.rounds.size} rodadas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Vencedores
            val sortedPlayers = game.players.sortedByDescending { it.totalPoints }
            val winners = sortedPlayers.take(2)
            
            Text(
                text = "🏆 Vencedores:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Gold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            winners.forEachIndexed { index, player ->
                Text(
                    text = "${index + 1}º ${player.name} (${player.totalPoints} pts)",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
} 