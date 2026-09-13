package com.estoque.bomdemais.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoriasRepository {
    private val categoriesRef = FirebaseDatabase.getInstance().getReference("categorias")
        .also { it.keepSynced(true) }
    private val productsRef = FirebaseDatabase.getInstance().getReference("produtos")

    fun categories(): Flow<List<Category>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val cats = snap.children.mapNotNull { it.getValue(Category::class.java) }
                trySend(cats)
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        categoriesRef.addValueEventListener(listener)
        awaitClose { categoriesRef.removeEventListener(listener) }
    }

    suspend fun addCategory(name: String): Boolean {
        val key = categoriesRef.push().key ?: return false
        return try {
            categoriesRef.child(key).setValue(Category(id = key, name = name)).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun deleteCategory(category: Category) {
        categoriesRef.child(category.id).removeValue().await()
        val products = productsRef.orderByChild("category").equalTo(category.name).get().await()
        products.children.forEach { it.ref.removeValue().await() }
    }

    suspend fun renameCategory(category: Category, newName: String): Boolean {
        return try {
            categoriesRef.child(category.id).child("name").setValue(newName).await()
            val products = productsRef.orderByChild("category").equalTo(category.name).get().await()
            val updates = products.children.mapNotNull { it.key }.associate { "$it/category" to newName }
            if (updates.isNotEmpty()) productsRef.updateChildren(updates).await()
            true
        } catch (e: Exception) { false }
    }
}
