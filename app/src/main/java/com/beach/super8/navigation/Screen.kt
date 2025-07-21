package com.beach.super8.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object GameCode : Screen("game_code")
    object PlayerRegistration : Screen("player_registration")
    object GamePlay : Screen("game_play")
    object GameHistory : Screen("game_history")
    object GameSpecificHistory : Screen("game_specific_history/{gameCode}") {
        fun createRoute(gameCode: String) = "game_specific_history/$gameCode"
    }
    object Ranking : Screen("ranking_final")
    object GeneralRanking : Screen("general_ranking")
} 