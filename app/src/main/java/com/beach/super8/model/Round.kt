package com.beach.super8.model

data class Round(
    val roundNumber: Int,
    val game1: GameMatch,
    val game2: GameMatch,
    var isCompleted: Boolean = false
)

data class GameMatch(
    val pair1: Pair<Player, Player>,
    val pair2: Pair<Player, Player>,
    var pair1Score: Int = 0,
    var pair2Score: Int = 0,
    var isCompleted: Boolean = false
) 