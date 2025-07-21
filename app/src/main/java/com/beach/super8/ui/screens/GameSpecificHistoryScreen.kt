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
import com.beach.super8.viewmodel.GameViewModel
import androidx.compose.ui.window.Dialog

@Composable
fun GameSpecificHistoryScreen(
    gameCode: String,
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val currentGame by viewModel.currentGame.collectAsState()
    val finishedGames by viewModel.finishedGames.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var editRoundIndex by remember { mutableStateOf(-1) }
    var editGameIndex by remember { mutableStateOf(-1) }
    var editGame by remember { mutableStateOf<GameMatch?>(null) }

    // Buscar o jogo pelo código
    val game = when {
        currentGame?.gameCode == gameCode -> currentGame
        else -> finishedGames.find { it.gameCode == gameCode }
    }

    if (game == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Torneio não encontrado", color = Color.Red, fontSize = 20.sp)
        }
        return
    }

    val isEditable = !game.isFinished

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
            // Botão voltar simples no topo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rodadas do Torneio ${game.gameCode}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlue
                )
            }
            // Lista de rodadas ocupando toda a tela
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(game.rounds.size) { roundIdx ->
                    val round = game.rounds[roundIdx]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "Rodada ${round.roundNumber}",
                                fontSize = 18.sp,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Jogo 1
                            GameHistoryRow(
                                gameMatch = round.game1,
                                isEditable = isEditable,
                                onEdit = {
                                    if (isEditable) {
                                        editRoundIndex = roundIdx
                                        editGameIndex = 0
                                        editGame = round.game1
                                        showEditDialog = true
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Jogo 2
                            GameHistoryRow(
                                gameMatch = round.game2,
                                isEditable = isEditable,
                                onEdit = {
                                    if (isEditable) {
                                        editRoundIndex = roundIdx
                                        editGameIndex = 1
                                        editGame = round.game2
                                        showEditDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        // Dialog de edição
        if (showEditDialog && editGame != null) {
            EditScoreDialogSpecific(
                game = editGame!!,
                gameIndex = editGameIndex,
                onScoreUpdated = { score1, score2 ->
                    // Atualizar placar no ViewModel
                    if (editRoundIndex >= 0 && editGameIndex >= 0) {
                        viewModel.updateRoundScore(editGameIndex, score1, score2)
                    }
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false }
            )
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

@Composable
private fun GameHistoryRow(
    gameMatch: GameMatch,
    isEditable: Boolean,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${gameMatch.pair1.first.name} & ${gameMatch.pair1.second.name}",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${gameMatch.pair1Score} x ${gameMatch.pair2Score}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )
        Text(
            text = "${gameMatch.pair2.first.name} & ${gameMatch.pair2.second.name}",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        if (isEditable) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryGreen)
            }
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Adicionar EditScoreDialog local
@Composable
fun EditScoreDialogSpecific(
    game: GameMatch,
    gameIndex: Int,
    onScoreUpdated: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var pair1Score by remember { mutableStateOf(game.pair1Score.toString()) }
    var pair2Score by remember { mutableStateOf(game.pair2Score.toString()) }
    
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
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Editar Placar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${game.pair1.first.name} & ${game.pair1.second.name} vs ${game.pair2.first.name} & ${game.pair2.second.name}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Dupla A",
                            fontSize = 12.sp,
                            color = PrimaryGreen
                        )
                        OutlinedTextField(
                            value = pair1Score,
                            onValueChange = { 
                                val filtered = it.filter { char -> char.isDigit() }
                                val score = filtered.toIntOrNull() ?: 0
                                if (score <= 6) {
                                    pair1Score = filtered
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                    
                    Text(
                        text = "VS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Dupla B",
                            fontSize = 12.sp,
                            color = OceanBlue
                        )
                        OutlinedTextField(
                            value = pair2Score,
                            onValueChange = { 
                                val filtered = it.filter { char -> char.isDigit() }
                                val score = filtered.toIntOrNull() ?: 0
                                if (score <= 6) {
                                    pair2Score = filtered
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OceanBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Mensagem de validação
                val totalScore = (pair1Score.toIntOrNull() ?: 0) + (pair2Score.toIntOrNull() ?: 0)
                Text(
                    text = when {
                        totalScore == 6 -> "✅ Total válido: $totalScore pontos"
                        totalScore < 6 -> "⚠️ Total: $totalScore pontos (precisa ser 6)"
                        totalScore > 6 -> "❌ Total: $totalScore pontos (máximo 6)"
                        else -> "📝 Digite os pontos (total deve ser 6)"
                    },
                    fontSize = 12.sp,
                    color = when {
                        totalScore == 6 -> PrimaryGreen
                        totalScore > 6 -> Color.Red
                        else -> Color.Gray
                    },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                        onClick = {
                            val score1 = pair1Score.toIntOrNull() ?: 0
                            val score2 = pair2Score.toIntOrNull() ?: 0
                            // Validação correta: total deve ser EXATAMENTE 6 pontos
                            if (score1 + score2 == 6 && score1 <= 6 && score2 <= 6) {
                                onScoreUpdated(score1, score2)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        // Botão só fica habilitado se total for EXATAMENTE 6
                        enabled = (pair1Score.toIntOrNull() ?: 0) + (pair2Score.toIntOrNull() ?: 0) == 6
                    ) {
                        Text(
                            text = "Salvar",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
} 