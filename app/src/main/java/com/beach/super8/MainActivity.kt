package com.beach.super8

import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.beach.super8.ui.screens.GameCodeScreen
import com.beach.super8.ui.screens.GamePlayScreen
import com.beach.super8.ui.screens.GameHistoryScreen
import com.beach.super8.ui.screens.HomeScreen
import com.beach.super8.ui.screens.PlayerRegistrationScreen
import com.beach.super8.ui.screens.RankingScreen
import com.beach.super8.ui.theme.Super8Theme
import com.beach.super8.viewmodel.GameViewModel
import com.beach.super8.ui.screens.GeneralRankingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("MainActivity", "=== MAIN ACTIVITY INICIADA ===")
            Log.d("MainActivity", "Context: $this")
            Log.d("MainActivity", "Intent: ${intent?.action}")
            
            enableEdgeToEdge()
            Log.d("MainActivity", "Edge to edge habilitado")
            
            setContent {
                Log.d("MainActivity", "Iniciando setContent")
                Super8Theme {
                    Log.d("MainActivity", "Super8Theme aplicado")
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Log.d("MainActivity", "Scaffold criado")
                        Super8App(
                            modifier = Modifier.padding(innerPadding),
                            context = this
                        )
                    }
                }
                Log.d("MainActivity", "setContent concluído")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "ERRO CRÍTICO NO ONCREATE: ${e.message}", e)
            Log.e("MainActivity", "Stack trace completo:", e)
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
    
    val viewModel: GameViewModel = viewModel()
    Log.d("Super8App", "GameViewModel criado")
    
    // Passar o contexto para o ViewModel
    LaunchedEffect(Unit) {
        try {
            Log.d("Super8App", "LaunchedEffect iniciado")
            viewModel.setContext(context)
            Log.d("Super8App", "Contexto definido no ViewModel")
        } catch (e: Exception) {
            Log.e("Super8App", "ERRO NO LAUNCHEDEFFECT: ${e.message}", e)
        }
    }
    
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
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de GamePlay")
                    navController.popBackStack()
                },
                onNavigateToRanking = {
                    Log.d("Super8App", "Navegando para Ranking")
                    navController.navigate(Screen.Ranking.route)
                },
                onNavigateToHome = {
                    Log.d("Super8App", "Voltando para Home do GamePlay")
                    navController.navigate(Screen.Home.route)
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
                }
            )
        }
        
        composable(
            route = "${Screen.GameSpecificHistory.route}",
            arguments = listOf(
                navArgument("gameCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameCode = backStackEntry.arguments?.getString("gameCode") ?: ""
            Log.d("Super8App", "=== RENDERIZANDO GAME SPECIFIC HISTORY SCREEN ===")
            com.beach.super8.ui.screens.GameSpecificHistoryScreen(
                gameCode = gameCode,
                viewModel = viewModel,
                onNavigateBack = {
                    Log.d("Super8App", "Voltando de GameSpecificHistory")
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Ranking.route) {
            Log.d("Super8App", "=== RENDERIZANDO RANKING SCREEN ===")
            RankingScreen(
                viewModel = viewModel,
                onNavigateToHistory = { gameCode ->
                    Log.d("Super8App", "Navegando para GameSpecificHistory com código $gameCode")
                    navController.navigate(Screen.GameSpecificHistory.createRoute(gameCode))
                },
                onNavigateToHome = {
                    Log.d("Super8App", "Voltando para Home do Ranking")
                    try {
                        // Navegação simples sem destruir a pilha
                        navController.navigate(Screen.Home.route) {
                            // Limpar apenas a pilha até a home, não destruir tudo
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    } catch (e: Exception) {
                        Log.e("Super8App", "Erro ao voltar para Home: ${e.message}", e)
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
    }
}