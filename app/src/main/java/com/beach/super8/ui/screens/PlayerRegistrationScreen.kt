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
import com.beach.super8.ui.theme.*
import com.beach.super8.viewmodel.GameViewModel

@Composable
fun PlayerRegistrationScreen(
    gameCode: String,
    viewModel: GameViewModel,
    onNavigateToGamePlay: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Log.d("PlayerRegistrationScreen", "=== PLAYER REGISTRATION SCREEN INICIADA ===")
    Log.d("PlayerRegistrationScreen", "GameCode recebido: '$gameCode'")
    
    val savedPlayers by viewModel.savedPlayers.collectAsState()
    var playerFields by remember { mutableStateOf(List(8) { "" }) }
    var showSortearDuplasDialog by remember { mutableStateOf(false) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    
    // Converter lista de Player para lista de nomes
    val savedPlayerNames = savedPlayers.map { it.name }
    
    // Log para verificar se a lista está sendo atualizada
    Log.d("PlayerRegistrationScreen", "=== LISTA DE JOGADORES ATUALIZADA ===")
    Log.d("PlayerRegistrationScreen", "SavedPlayers size: ${savedPlayers.size}")
    Log.d("PlayerRegistrationScreen", "SavedPlayerNames: $savedPlayerNames")
    
    // Filtrar jogadores já selecionados da lista
    val availablePlayerNames = savedPlayerNames.filter { playerName ->
        !playerFields.contains(playerName)
    }
    
    Log.d("PlayerRegistrationScreen", "AvailablePlayerNames: $availablePlayerNames")
    
    // Função para atualizar campo específico
    val updatePlayerField: (Int, String) -> Unit = { index, playerName ->
        try {
            Log.d("PlayerRegistrationScreen", "=== ATUALIZANDO CAMPO $index: '$playerName' ===")
            playerFields = playerFields.toMutableList().apply { this[index] = playerName }
            Log.d("PlayerRegistrationScreen", "=== CAMPO ATUALIZADO ===")
        } catch (e: Exception) {
            Log.e("PlayerRegistrationScreen", "ERRO AO ATUALIZAR CAMPO: ${e.message}", e)
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
                // Game Code Info
                GameCodeInfoCard(gameCode)
            }
            
            item {
                // Add Player Button
                AddPlayerButtonSection(
                    onAddPlayer = {
                        try {
                            Log.d("PlayerRegistrationScreen", "=== ABRINDO DIALOG CADASTRAR ATLETA ===")
                            showAddPlayerDialog = true
                        } catch (e: Exception) {
                            Log.e("PlayerRegistrationScreen", "ERRO AO ABRIR DIALOG: ${e.message}", e)
                        }
                    }
                )
            }
            
            item {
                // Player Selection Fields
                PlayerSelectionFieldsSection(
                    playerFields = playerFields,
                    availablePlayerNames = availablePlayerNames,
                    onPlayerFieldUpdate = updatePlayerField
                )
            }
            
            item {
                // Action Buttons
                ActionButtonsSection(
                    playerFields = playerFields,
                    onSortearDuplas = {
                        try {
                            Log.d("PlayerRegistrationScreen", "=== BOTÃO SORTEAR DUPLAS CLICADO ===")
                            showSortearDuplasDialog = true
                        } catch (e: Exception) {
                            Log.e("PlayerRegistrationScreen", "ERRO AO ABRIR DIALOG: ${e.message}", e)
                        }
                    },
                    onBack = {
                        try {
                            Log.d("PlayerRegistrationScreen", "=== BOTÃO VOLTAR CLICADO ===")
                            onNavigateBack()
                            Log.d("PlayerRegistrationScreen", "=== VOLTA EXECUTADA ===")
                        } catch (e: Exception) {
                            Log.e("PlayerRegistrationScreen", "ERRO AO VOLTAR: ${e.message}", e)
                        }
                    }
                )
            }
        }
    }
    
    // Add Player Dialog
    if (showAddPlayerDialog) {
        AddPlayerDialog(
            onPlayerAdded = { playerName ->
                try {
                    Log.d("PlayerRegistrationScreen", "=== NOVO ATLETA ADICIONADO: $playerName ===")
                    viewModel.addPlayer(playerName)
                    showAddPlayerDialog = false
                } catch (e: Exception) {
                    Log.e("PlayerRegistrationScreen", "ERRO AO ADICIONAR ATLETA: ${e.message}", e)
                }
            },
            onDismiss = {
                try {
                    Log.d("PlayerRegistrationScreen", "=== CANCELANDO CADASTRO DE ATLETA ===")
                    showAddPlayerDialog = false
                } catch (e: Exception) {
                    Log.e("PlayerRegistrationScreen", "ERRO AO CANCELAR: ${e.message}", e)
                }
            }
        )
    }
    
    // Sortear Duplas Dialog
    if (showSortearDuplasDialog) {
        SortearDuplasDialog(
            onConfirm = {
                try {
                    Log.d("PlayerRegistrationScreen", "=== CONFIRMANDO SORTEAR DUPLAS ===")
                    
                    // Criar lista de jogadores usando os salvos
                    val players = playerFields.filter { it.isNotBlank() }.map { playerName ->
                        val trimmedName = playerName.trim()
                        // Buscar jogador já salvo para preservar pontos
                        val savedPlayer = savedPlayers.find { 
                            it.name.trim().equals(trimmedName, ignoreCase = true) 
                        }
                        
                        if (savedPlayer != null) {
                            Log.d("PlayerRegistrationScreen", "Usando jogador salvo: ${savedPlayer.name} com ${savedPlayer.totalPoints} pontos")
                            savedPlayer
                        } else {
                            Log.e("PlayerRegistrationScreen", "ERRO: Jogador não encontrado: $trimmedName")
                            com.beach.super8.model.Player(name = trimmedName)
                        }
                    }
                    
                    Log.d("PlayerRegistrationScreen", "Lista final de jogadores: ${players.map { "${it.name}: ${it.totalPoints} pontos" }}")
                    viewModel.createNewGame(gameCode, players)
                    showSortearDuplasDialog = false
                    onNavigateToGamePlay()
                } catch (e: Exception) {
                    Log.e("PlayerRegistrationScreen", "ERRO AO CONFIRMAR: ${e.message}", e)
                }
            },
            onDismiss = {
                try {
                    Log.d("PlayerRegistrationScreen", "=== CANCELANDO SORTEAR DUPLAS ===")
                    showSortearDuplasDialog = false
                } catch (e: Exception) {
                    Log.e("PlayerRegistrationScreen", "ERRO AO CANCELAR: ${e.message}", e)
                }
            }
        )
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
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Selecionar Atletas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "Escolha 8 atletas para o jogo",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun GameCodeInfoCard(gameCode: String) {
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
                    text = "Código do Jogo",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = gameCode,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun AddPlayerButtonSection(
    onAddPlayer: () -> Unit
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
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Cadastrar Atleta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Adicione novos jogadores ao sistema",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = onAddPlayer,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OceanBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerSelectionFieldsSection(
    playerFields: List<String>,
    availablePlayerNames: List<String>,
    onPlayerFieldUpdate: (Int, String) -> Unit
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
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Atletas Selecionados (8)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 8 campos de digitação
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (i in 0 until playerFields.size) {
                    PlayerInputField(
                        playerNumber = i + 1,
                        playerName = playerFields[i],
                        availablePlayerNames = availablePlayerNames,
                        onPlayerFieldUpdate = onPlayerFieldUpdate,
                        onSavePlayer = { /* This function is no longer needed */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerInputField(
    playerNumber: Int,
    playerName: String,
    availablePlayerNames: List<String>,
    onPlayerFieldUpdate: (Int, String) -> Unit,
    onSavePlayer: () -> Unit
) {
    var showPlayerListDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LightGreen.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Number Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playerNumber.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Player Name Display (read-only)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (playerName.isNotBlank()) playerName else "Selecione um atleta",
                    fontSize = 16.sp,
                    color = if (playerName.isNotBlank()) Color.Black else Color.Gray,
                    fontWeight = if (playerName.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Select Player Button
            IconButton(
                onClick = { 
                    try {
                        Log.d("PlayerInputField", "=== ABRINDO LISTA DE ATLETAS PARA CAMPO $playerNumber ===")
                        showPlayerListDialog = true
                    } catch (e: Exception) {
                        Log.e("PlayerInputField", "ERRO AO ABRIR LISTA: ${e.message}", e)
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OceanBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Player List Dialog
    if (showPlayerListDialog) {
        PlayerListDialog(
            availablePlayerNames = availablePlayerNames,
            onPlayerSelected = { selectedPlayer ->
                try {
                    Log.d("PlayerInputField", "=== ATLETA SELECIONADO DA LISTA: $selectedPlayer PARA CAMPO $playerNumber ===")
                    onPlayerFieldUpdate(playerNumber - 1, selectedPlayer)
                    showPlayerListDialog = false
                    Log.d("PlayerInputField", "=== DIALOG FECHADO ===")
                } catch (e: Exception) {
                    Log.e("PlayerInputField", "ERRO AO SELECIONAR DA LISTA: ${e.message}", e)
                }
            },
            onDismiss = { 
                try {
                    Log.d("PlayerInputField", "=== CANCELANDO DIALOG ===")
                    showPlayerListDialog = false
                } catch (e: Exception) {
                    Log.e("PlayerInputField", "ERRO AO CANCELAR DIALOG: ${e.message}", e)
                }
            }
        )
    }
}

@Composable
private fun ActionButtonsSection(
    playerFields: List<String>,
    onSortearDuplas: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val canStartGame = playerFields.all { it.isNotBlank() }
        
        // Start Game Button
        Button(
            onClick = {
                if (canStartGame) {
                    onSortearDuplas()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp),
            enabled = canStartGame
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sortear Duplas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Back Button
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
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
                    text = "Voltar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 

@Composable
private fun AddPlayerDialog(
    onPlayerAdded: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    
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
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Adicionar Jogador",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Digite o nome do jogador que deseja adicionar.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    placeholder = {
                        Text(
                            text = "Nome do jogador",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedLabelColor = PrimaryGreen,
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
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
                        onClick = {
                            if (playerName.isNotBlank()) {
                                onPlayerAdded(playerName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        enabled = playerName.isNotBlank()
                    ) {
                        Text(
                            text = "Adicionar",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
} 

@Composable
private fun SortearDuplasDialog(
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
                        .background(Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Sortear Duplas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "As duplas serão formadas seguindo o sistema oficial do Super 8!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mostrar exemplo da primeira rodada
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGreen.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Exemplo - Rodada 1:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Jogo 1: Jogador 1 & Jogador 5 vs Jogador 7 & Jogador 8",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = "• Jogo 2: Jogador 2 & Jogador 3 vs Jogador 4 & Jogador 6",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "7 rodadas com 2 jogos cada = 14 jogos no total!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = OceanBlue
                        )
                    }
                }
                
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
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Sortear!",
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
} 

@Composable
private fun PlayerListDialog(
    availablePlayerNames: List<String>,
    onPlayerSelected: (String) -> Unit,
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
                modifier = Modifier.padding(24.dp)
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
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Selecionar Atleta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Atletas Disponíveis",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (availablePlayerNames.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum atleta disponível",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availablePlayerNames) { playerName ->
                            Card(
                                onClick = { 
                                    try {
                                        Log.d("PlayerListDialog", "=== JOGADOR SELECIONADO: $playerName ===")
                                        onPlayerSelected(playerName)
                                        Log.d("PlayerListDialog", "=== CHAMANDO CALLBACK ===")
                                    } catch (e: Exception) {
                                        Log.e("PlayerListDialog", "ERRO AO SELECIONAR: ${e.message}", e)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = LightGreen.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Text(
                                        text = playerName,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 16.sp,
                        color = OceanBlue
                    )
                }
            }
        }
    }
} 