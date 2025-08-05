package com.beach.super8.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import com.beach.super8.R
import com.beach.super8.ui.theme.*
import com.beach.super8.viewmodel.PostgresGameViewModel

@Composable
fun GameCodeScreen(
    viewModel: PostgresGameViewModel,
    onNavigateToPlayerRegistration: (String) -> Unit,
    onNavigateToGamePlay: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Log.d("GameCodeScreen", "=== GAME CODE SCREEN INICIADA ===")
    
    var gameCode by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
                // Game Code Input Card
                GameCodeInputCard(
                    gameCode = gameCode,
                    onGameCodeChange = { gameCode = it }
                )
            }
            
            item {
                // Action Buttons
                ActionButtonsSection(
                    gameCode = gameCode,
                    onCreateNewGame = {
                        try {
                            Log.d("GameCodeScreen", "Criando novo jogo")
                            val newGameCode = if (gameCode.isBlank()) {
                                // Gerar código único automaticamente
                                val uniqueCode = viewModel.generateUniqueGameCode()
                                Log.d("GameCodeScreen", "Código único gerado: $uniqueCode")
                                uniqueCode
                            } else {
                                // Verificar se código digitado já existe
                                if (viewModel.gameCodeExists(gameCode)) {
                                    errorMessage = "Este código já existe! Digite outro código."
                                    showErrorDialog = true
                                    return@ActionButtonsSection
                                }
                                Log.d("GameCodeScreen", "Usando código digitado: $gameCode")
                                gameCode
                            }
                            Log.d("GameCodeScreen", "Navegando para cadastro com código: $newGameCode")
                            onNavigateToPlayerRegistration(newGameCode)
                        } catch (e: Exception) {
                            Log.e("GameCodeScreen", "Erro ao criar novo jogo", e)
                            errorMessage = "Erro ao criar jogo: ${e.message}"
                            showErrorDialog = true
                        }
                    },
                    onEnterGame = {
                        try {
                            Log.d("GameCodeScreen", "Tentando entrar em jogo com código: $gameCode")
                            if (gameCode.isBlank()) {
                                errorMessage = "Digite um código para entrar no jogo!"
                                showErrorDialog = true
                                return@ActionButtonsSection
                            }
                            
                            // Validar código
                            when (viewModel.validateGameCode(gameCode)) {
                                PostgresGameViewModel.GameCodeValidation.EMPTY -> {
                                    errorMessage = "Digite um código para entrar no jogo!"
                                    showErrorDialog = true
                                }
                                PostgresGameViewModel.GameCodeValidation.NOT_FOUND -> {
                                    errorMessage = "Código não encontrado! Verifique o código digitado."
                                    showErrorDialog = true
                                }
                                PostgresGameViewModel.GameCodeValidation.FINISHED -> {
                                    errorMessage = "Este jogo já foi finalizado! Não é possível entrar."
                                    showErrorDialog = true
                                }
                                PostgresGameViewModel.GameCodeValidation.VALID -> {
                                    Log.d("GameCodeScreen", "Código válido, carregando jogo...")
                                    val loadedGame = viewModel.loadGameByCode(gameCode)
                                    if (loadedGame != null) {
                                        Log.d("GameCodeScreen", "Jogo carregado com sucesso, navegando para jogo")
                                        onNavigateToGamePlay(gameCode)
                                    } else {
                                        Log.e("GameCodeScreen", "Erro ao carregar jogo")
                                        errorMessage = "Erro ao carregar jogo. Tente novamente."
                                        showErrorDialog = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("GameCodeScreen", "Erro ao entrar em jogo", e)
                            errorMessage = "Erro ao entrar no jogo: ${e.message}"
                            showErrorDialog = true
                        }
                    },
                    onBack = {
                        try {
                            Log.d("GameCodeScreen", "Voltando para tela anterior")
                            onNavigateBack()
                        } catch (e: Exception) {
                            Log.e("GameCodeScreen", "Erro ao voltar", e)
                        }
                    }
                )
            }
        }
    }
    
    // Error Dialog
    if (showErrorDialog) {
        ErrorDialog(
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
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
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = stringResource(R.string.game_code),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = stringResource(R.string.enter_code_description),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun GameCodeInputCard(
    gameCode: String,
    onGameCodeChange: (String) -> Unit
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
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OceanBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = stringResource(R.string.game_code),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = gameCode,
                onValueChange = onGameCodeChange,
                placeholder = {
                    Text(
                        text = "Ex: 12345",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    focusedLabelColor = PrimaryGreen,
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Deixe em branco para gerar um código automático",
                        fontSize = 12.sp,
                        color = OceanBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    gameCode: String,
    onCreateNewGame: () -> Unit,
    onEnterGame: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Criar Novo Jogo - Button principal
        Button(
            onClick = onCreateNewGame,
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
                        imageVector = Icons.Default.Casino,
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
                        text = stringResource(R.string.create_new_game),
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
        
        // Entrar em Jogo - Button secundário
        Button(
            onClick = onEnterGame,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
            shape = RoundedCornerShape(12.dp),
            enabled = gameCode.isNotBlank()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = stringResource(R.string.enter_game),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Voltar Button
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
                    text = stringResource(R.string.back),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 

@Composable
private fun ErrorDialog(
    message: String,
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
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Erro",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "OK",
                        color = Color.White
                    )
                }
            }
        }
    }
} 