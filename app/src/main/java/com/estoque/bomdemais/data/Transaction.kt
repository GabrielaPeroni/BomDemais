package com.estoque.bomdemais.data

data class Transaction(
    val id: String = "",
    val type: String = "RECEITA",
    val amount: Double = 0.0,
    val description: String = "",
    val date: Long = 0,
    val monthKey: String = ""
)
