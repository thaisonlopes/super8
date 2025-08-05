package com.beach.super8

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.beach.super8.navigation.Screen
import com.beach.super8.ui.screens.*
import com.beach.super8.ui.theme.Super8Theme
import com.beach.super8.viewmodel.PostgresGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.d("MainActivity", "=== MAIN ACTIVITY INICIADA ===")
        
        setContent {
            Super8Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Super8App(
                        modifier = Modifier,
                        context = this
                    )
                }
            }
        }
    }
}

@Composable
fun Super8App(
    modifier: Modifier = Modifier,
    context: MainActivity
) {
    Log.d("Super8App", "=== SUPER8APP INICIADA ===")
    val navController = rememberNavController()
    Log.d("Super8App", "NavController criado")
    
    val viewModel: PostgresGameViewModel = viewModel()
    Log.d("Super8App", "PostgresGameViewModel criado")
    
    Log.d("Super8App", "Iniciando NavHost")
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        Log.d("Super8App", "NavHost criado")
        
        composable(Screen.Home.route) {
            Log.d("Super8App", "=== RENDERIZANDO HOME SCREEN ===")
            HomeScreen(
                viewModel = viewModel,
                onNavigateToGameCode = {
                    try {
                        Log.d("Super8App", "=== NAVEGANDO PARA GAME CODE ===")
                        navController.navigate(Screen.GameCode.route)
                        Log.d("Super8App", "=== NAVEGAÇÃO CONCLUÍDA ===")
                    } catch (e: Exception) {
                        Log.e("Super8App", "ERRO NA NAVEGAÇÃO: ${e.message}", e)
                    }
                },
                onNavigateToGameHistory = {
                    Log.d("Super8App", "Navegando para GameHistory")
                    navController.navigate(Screen.GameHistory.route)
                },
                onNavigateToRanking = {
                    Log.d("Super8App", "Navegando para Ranking Geral")
                    navController.navigate(Screen.GeneralRanking.route)
                }
            )
        }
        
        composable(Screen.GameCode.route) {
            Log.d("Super8App", "=== RENDERIZANDO GAME CODE SCREEN ===")
            GameCodeScreen(
                viewModel = viewModel,
                onNavigateToPlayerRegistration = { gameCode ->
                    try {
                        Log.d("Super8App", "Navegando para PlayerRegistration com código: $gameCode")
                        navController.navigate("${Screen.PlayerRegistration.route}/$gameCode")
                    } catch (e: Exception) {
                        Log.e("Super8App", "Erro ao navegar para PlayerRegistration", e)
                    }
                },
                onNavigateToGamePlay = { gameCode ->
                    try {
                        Log.d("Super8App", "Navegando para GamePlay com código: $gameCode")
                        navController.navigate(Screen.GamePlay.route)
                    } catch (e: Exception) {
                        Log.e("Super8App", "Erro ao navegar para GamePlay", e)
                    }
                },
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de GameCode")
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "${Screen.PlayerRegistration.route}/{gameCode}",
            arguments = listOf(
                navArgument("gameCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameCode = backStackEntry.arguments?.getString("gameCode") ?: ""
            Log.d("Super8App", "=== RENDERIZANDO PLAYER REGISTRATION SCREEN ===")
            PlayerRegistrationScreen(
                gameCode = gameCode,
                viewModel = viewModel,
                onNavigateToGamePlay = {
                    Log.d("Super8App", "Navegando para GamePlay")
                    navController.navigate(Screen.GamePlay.route)
                },
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de PlayerRegistration")
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.GamePlay.route) {
            Log.d("Super8App", "=== RENDERIZANDO GAME PLAY SCREEN ===")
            GamePlayScreen(
                viewModel = viewModel,
                onNavigateToRanking = {
                    Log.d("Super8App", "Navegando para Ranking")
                    navController.navigate(Screen.Ranking.route)
                },
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de GamePlay")
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    Log.d("Super8App", "Navegando para Home")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Ranking.route) {
            Log.d("Super8App", "=== RENDERIZANDO RANKING SCREEN ===")
            RankingScreen(
                viewModel = viewModel,
                onNavigateToHistory = { gameCode ->
                    Log.d("Super8App", "Navegando para GameSpecificHistory com código: $gameCode")
                    navController.navigate("${Screen.GameSpecificHistory.route}/$gameCode")
                },
                onNavigateToHome = {
                    Log.d("Super8App", "Navegando para Home")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.GeneralRanking.route) {
            Log.d("Super8App", "=== RENDERIZANDO GENERAL RANKING SCREEN ===")
            GeneralRankingScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de GeneralRanking")
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.GameHistory.route) {
            Log.d("Super8App", "=== RENDERIZANDO GAME HISTORY SCREEN ===")
            GameHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de GameHistory")
                    navController.popBackStack()
                },
                onNavigateToGameDetails = { gameCode ->
                    Log.d("Super8App", "Navegando para GameSpecificHistory com código: $gameCode")
                    navController.navigate("${Screen.GameSpecificHistory.route}/$gameCode")
                }
            )
        }
        
        composable(
            route = "${Screen.GameSpecificHistory.route}/{gameCode}",
            arguments = listOf(
                navArgument("gameCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameCode = backStackEntry.arguments?.getString("gameCode") ?: ""
            Log.d("Super8App", "=== RENDERIZANDO GAME SPECIFIC HISTORY SCREEN ===")
            GameSpecificHistoryScreen(
                gameCode = gameCode,
                viewModel = viewModel,
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de GameSpecificHistory")
                    navController.popBackStack()
                }
            )
        }
    }
    
    Log.d("Super8App", "=== SUPER8APP CONCLUÍDA ===")
}