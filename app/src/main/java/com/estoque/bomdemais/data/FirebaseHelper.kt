package com.estoque.bomdemais.data

import android.util.Log
import com.google.firebase.database.*

class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance()
    private val categoriesRef = database.getReference("categorias")
    private val productsRef = database.getReference("produtos")
    private val notasRef = database.getReference("notas")
    private val listaComprasRef = database.getReference("lista_compras")

    init {
        categoriesRef.keepSynced(true)
        productsRef.keepSynced(true)
        notasRef.keepSynced(true)
        listaComprasRef.keepSynced(true)
    }

    fun addCategoria(category: String, callback: (Boolean) -> Unit) {
        val categoryId = categoriesRef.push().key
        if (categoryId != null) {
            categoriesRef.child(categoryId).setValue(category)
                .addOnCompleteListener { task -> callback(task.isSuccessful) }
        } else {
            callback(false)
        }
    }

    fun listenToCategories(callback: (List<String>) -> Unit): () -> Unit {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { categories.add(it) }
                }
                callback(categories)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load categories: ${error.message}")
            }
        }
        categoriesRef.addValueEventListener(listener)
        return { categoriesRef.removeEventListener(listener) }
    }

    fun addProduct(name: String, category: String, callback: (Product?) -> Unit) {
        val productId = productsRef.push().key ?: return callback(null)
        val product = Product(id = productId, name = name, category = category, quantity = 0)
        productsRef.child(productId).setValue(product)
            .addOnCompleteListener { task -> callback(if (task.isSuccessful) product else null) }
    }

    fun listenToProductsByCategory(category: String, callback: (List<Product>) -> Unit): () -> Unit {
        val query = productsRef.orderByChild("category").equalTo(category)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Product>()
                for (child in snapshot.children) {
                    child.getValue(Product::class.java)?.let { products.add(it) }
                }
                callback(products)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load products: ${error.message}")
            }
        }
        query.addValueEventListener(listener)
        return { query.removeEventListener(listener) }
    }

    fun addShoppingItem(item: ShoppingItem, callback: (Boolean) -> Unit) {
        val key = listaComprasRef.push().key ?: return callback(false)
        val withId = item.copy(id = key)
        listaComprasRef.child(key).setValue(withId)
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun listenToShoppingItems(callback: (List<ShoppingItem>) -> Unit): () -> Unit {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ShoppingItem>()
                for (child in snapshot.children) {
                    child.getValue(ShoppingItem::class.java)?.let { items.add(it) }
                }
                callback(items)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load lista: ${error.message}")
            }
        }
        listaComprasRef.addValueEventListener(listener)
        return { listaComprasRef.removeEventListener(listener) }
    }

    fun deleteShoppingItem(itemId: String) {
        listaComprasRef.child(itemId).removeValue()
    }

    fun updateShoppingItemQuantity(item: ShoppingItem) {
        listaComprasRef.child(item.id).child("quantityToBuy").setValue(item.quantityToBuy)
    }

    fun listenToNotes(callback: (List<Note>) -> Unit): () -> Unit {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                for (child in snapshot.children) {
                    child.getValue(Note::class.java)?.let { notes.add(it) }
                }
                notes.sortByDescending { it.timestamp }
                callback(notes)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load notes: ${error.message}")
            }
        }
        notasRef.addValueEventListener(listener)
        return { notasRef.removeEventListener(listener) }
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

    fun updateShoppingItemChecked(item: ShoppingItem) {
        listaComprasRef.child(item.id).child("isChecked").setValue(item.isChecked)
    }

    fun restoreShoppingItem(item: ShoppingItem) {
        listaComprasRef.child(item.id).setValue(item)
    }

    fun restoreNote(note: Note) {
        notasRef.child(note.id).setValue(note)
    }

    fun deleteProduct(productId: String) {
        productsRef.child(productId).removeValue()
    }

    fun restoreProduct(product: Product) {
        productsRef.child(product.id).setValue(product)
    }

    fun renameProduct(product: Product, newName: String) {
        productsRef.child(product.id).child("name").setValue(newName)
    }

    fun renameNote(note: Note, newText: String) {
        notasRef.child(note.id).child("text").setValue(newText)
    }

    fun renameShoppingItem(item: ShoppingItem, newName: String) {
        listaComprasRef.child(item.id).child("name").setValue(newName)
    }

    fun deleteCategory(name: String) {
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    if (child.getValue(String::class.java) == name) {
                        child.ref.removeValue()
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        productsRef.orderByChild("category").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) child.ref.removeValue()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun renameCategory(oldName: String, newName: String, callback: (Boolean) -> Unit) {
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var categoryKey: String? = null
                for (child in snapshot.children) {
                    if (child.getValue(String::class.java) == oldName) {
                        categoryKey = child.key
                        break
                    }
                }
                if (categoryKey == null) { callback(false); return }
                categoriesRef.child(categoryKey).setValue(newName)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) { callback(false); return@addOnCompleteListener }
                        productsRef.orderByChild("category").equalTo(oldName)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(productSnapshot: DataSnapshot) {
                                    val updates = mutableMapOf<String, Any>()
                                    for (child in productSnapshot.children) {
                                        child.key?.let { updates["$it/category"] = newName }
                                    }
                                    if (updates.isEmpty()) { callback(true); return }
                                    productsRef.updateChildren(updates)
                                        .addOnCompleteListener { callback(it.isSuccessful) }
                                }
                                override fun onCancelled(error: DatabaseError) { callback(false) }
                            })
                    }
            }
            override fun onCancelled(error: DatabaseError) { callback(false) }
        })
    }

}
