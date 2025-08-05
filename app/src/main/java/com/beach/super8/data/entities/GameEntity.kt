package com.beach.super8.data.entities

import java.util.Date

data class GameEntity(
    val id: Int = 0,  // Mudou de String para Int
    val codigoJogo: String,  // codigo_jogo
    val finalizado: Boolean = false,  // finalizado
    val dataCriacao: Date = Date(),  // data_criacao
    val dataAtualizacao: Date = Date()  // data_atualizacao
) 