package com.beach.super8.model

data class Match(
    val id: Int = 0,
    val round: Int,
    val player1: Player,
    val player2: Player,
    val player1Score: Int? = null,
    val player2Score: Int? = null,
    val isFinished: Boolean = false
) 