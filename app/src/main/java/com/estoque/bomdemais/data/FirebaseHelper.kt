package com.estoque.bomdemais.data

import com.google.firebase.database.*

class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("produtos")

    // Adicionar produto ao Firebase
    fun addProduct(name: String, category: String) {
        val productId = productsRef.push().key  // Gera um ID único
        val product = Product(name, category)
        if (productId != null) {
            productsRef.child(productId).setValue(product)
        }
    }

    // Buscar produtos do Firebase
    fun getProducts(callback: (List<Product>) -> Unit) {
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let { products.add(it) }
                }
                callback(products)
            }

            override fun onCancelled(error: DatabaseError) {
                // Tratar erros aqui
            }
        })
    }

}
