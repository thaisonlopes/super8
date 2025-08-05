package com.beach.super8.data.entities

import java.util.Date

data class PlayerEntity(
    val id: Int = 0,  // Mudou de String para Int
    val nome: String,  // nome
    val dataCriacao: Date = Date(),  // data_criacao
    val dataAtualizacao: Date = Date()  // data_atualizacao
) 