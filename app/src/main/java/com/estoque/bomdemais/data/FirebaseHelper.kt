package com.estoque.bomdemais.data

import android.util.Log
import com.google.firebase.database.*

class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance()
    private val categoriesRef = database.getReference("categorias")
    private val productsRef = database.getReference("produtos")
    private val notasRef = database.getReference("notas")

    fun addCategoria(category: String, callback: (Boolean) -> Unit) {
        val categoryId = categoriesRef.push().key
        if (categoryId != null) {
            categoriesRef.child(categoryId).setValue(category)
                .addOnCompleteListener { task -> callback(task.isSuccessful) }
        } else {
            callback(false)
        }
    }

    fun getCategorias(callback: (List<String>) -> Unit) {
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    categorySnapshot.getValue(String::class.java)?.let { categories.add(it) }
                }
                callback(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load categories: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun addProduct(name: String, category: String) {
        val productId = productsRef.push().key
        val product = Product(id = productId ?: "", name = name, category = category)
        if (productId != null) {
            productsRef.child(productId).setValue(product)
        }
    }

    fun getProductsByCategory(category: String, callback: (List<Product>) -> Unit) {
        productsRef.orderByChild("category").equalTo(category)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children) {
                        productSnapshot.getValue(Product::class.java)?.let { products.add(it) }
                    }
                    callback(products)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Failed to load products: ${error.message}")
                    callback(emptyList())
                }
            })
    }

    fun moveProductToLackCategory(product: Product) {
        productsRef.child(product.id).child("category").setValue(PRODUTOS_EM_FALTA)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FirebaseHelper", "Failed to move product: ${task.exception?.message}")
                }
            }
    }

    fun getNotes(callback: (List<Note>) -> Unit) {
        notasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                for (noteSnapshot in snapshot.children) {
                    noteSnapshot.getValue(Note::class.java)?.let { notes.add(it) }
                }
                notes.sortByDescending { it.timestamp }
                callback(notes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load notes: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun addNote(text: String, callback: (Note?) -> Unit) {
        val noteId = notasRef.push().key
        if (noteId != null) {
            val note = Note(id = noteId, text = text, timestamp = System.currentTimeMillis())
            notasRef.child(noteId).setValue(note)
                .addOnCompleteListener { task ->
                    callback(if (task.isSuccessful) note else null)
                }
        } else {
            callback(null)
        }
    }

    fun deleteNote(noteId: String) {
        notasRef.child(noteId).removeValue()
    }

    fun updateProductQuantity(product: Product) {
        productsRef.child(product.id).child("quantity").setValue(product.quantity)
    }

    companion object {
        const val PRODUTOS_EM_FALTA = "Produtos em falta"
    }
}
