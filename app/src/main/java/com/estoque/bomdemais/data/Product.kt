package com.estoque.bomdemais.data

data class Product(
    var id: String = "",  // Adicione um identificador único
    val name: String = "",
    val category: String = "",
    val description: String = "",
    var quantity: Int = 1
)
