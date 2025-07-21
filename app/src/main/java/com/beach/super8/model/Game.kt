package com.beach.super8.model

data class Game(
    val id: String = System.currentTimeMillis().toString(),
    val gameCode: String,
    val date: Long = System.currentTimeMillis(),
    val players: List<Player> = emptyList(),
    val rounds: List<Round> = emptyList(),
    var currentRound: Int = 1,
    var isFinished: Boolean = false,
    var winner: Player? = null
) 