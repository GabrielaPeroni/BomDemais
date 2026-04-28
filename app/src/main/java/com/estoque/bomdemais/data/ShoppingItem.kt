package com.estoque.bomdemais.data

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    var quantityToBuy: Int = 1
)
