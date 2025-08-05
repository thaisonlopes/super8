package com.beach.super8.data.entities

import java.util.Date

data class PontuacaoJogoEntity(
    val id: Long = 0,
    val idJogo: Int,  // Mudou de String para Int
    val idJogador: Int,  // id_jogador
    val pontuacao: Int = 0,  // pontuacao
    val rodada: Int = 0,  // rodada
    val dataCriacao: Date = Date()  // data_criacao
) 