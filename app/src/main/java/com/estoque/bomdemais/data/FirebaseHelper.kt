package com.estoque.bomdemais.data

import android.util.Log
import com.google.firebase.database.*

class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance()
    private val categoriesRef = database.getReference("categorias")
    private val productsRef = database.getReference("produtos")
    private val notasRef = database.getReference("notas")
    private val listaComprasRef = database.getReference("lista_compras")

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

    fun addProduct(name: String, category: String, callback: (Product?) -> Unit) {
        val productId = productsRef.push().key ?: return callback(null)
        val product = Product(id = productId, name = name, category = category, quantity = 0)
        productsRef.child(productId).setValue(product)
            .addOnCompleteListener { task -> callback(if (task.isSuccessful) product else null) }
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

    fun addShoppingItem(item: ShoppingItem, callback: (Boolean) -> Unit) {
        val key = listaComprasRef.push().key ?: return callback(false)
        val withId = item.copy(id = key)
        listaComprasRef.child(key).setValue(withId)
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun getShoppingItems(callback: (List<ShoppingItem>) -> Unit) {
        listaComprasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ShoppingItem>()
                for (child in snapshot.children) {
                    child.getValue(ShoppingItem::class.java)?.let { items.add(it) }
                }
                callback(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load lista: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun deleteShoppingItem(itemId: String) {
        listaComprasRef.child(itemId).removeValue()
    }

    fun updateShoppingItemQuantity(item: ShoppingItem) {
        listaComprasRef.child(item.id).child("quantityToBuy").setValue(item.quantityToBuy)
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

}
