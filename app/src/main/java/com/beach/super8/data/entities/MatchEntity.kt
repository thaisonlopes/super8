package com.beach.super8.data.entities

import java.util.Date

data class MatchEntity(
    val id: Long = 0,
    val idJogo: Int,  // Mudou de String para Int
    val rodada: Int,  // rodada
    val idJogador1: Int,  // Mudou de String para Int
    val idJogador2: Int,  // Mudou de String para Int
    val pontuacaoJogador1: Int? = null,  // pontuacao_jogador1
    val pontuacaoJogador2: Int? = null,  // pontuacao_jogador2
    val finalizada: Boolean = false,  // finalizada
    val dataCriacao: Date = Date()  // data_criacao
) 