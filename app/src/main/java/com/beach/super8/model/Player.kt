package com.beach.super8.model

import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var totalPoints: Int = 0,
    var gamesPlayed: Int = 0,
    var wins: Int = 0,
    var losses: Int = 0
) 