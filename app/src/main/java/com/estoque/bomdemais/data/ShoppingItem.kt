package com.estoque.bomdemais.data

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    var quantity: Int = 1,
    val unit: String = "un",
    var isChecked: Boolean = false
)
