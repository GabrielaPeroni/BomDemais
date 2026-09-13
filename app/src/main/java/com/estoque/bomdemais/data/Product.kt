package com.estoque.bomdemais.data

data class Product(
    var id: String = "",
    val name: String = "",
    val category: String = "",
    var quantity: Int = 0,
    val unit: String = "un",
    val minQuantity: Int = 1
)
