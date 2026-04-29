package com.estoque.bomdemais.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProdutosRepository {
    private val ref = FirebaseDatabase.getInstance().getReference("produtos")
        .also { it.keepSynced(true) }

    fun productsByCategory(category: String): Flow<List<Product>> = callbackFlow {
        val query = ref.orderByChild("category").equalTo(category)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val products = snap.children.mapNotNull { it.getValue(Product::class.java) }
                trySend(products)
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addProduct(name: String, category: String): Product? {
        val key = ref.push().key ?: return null
        val product = Product(id = key, name = name, category = category, quantity = 0)
        return try { ref.child(key).setValue(product).await(); product }
        catch (e: Exception) { null }
    }

    suspend fun deleteProduct(id: String) = ref.child(id).removeValue().await()
    suspend fun restoreProduct(product: Product) = ref.child(product.id).setValue(product).await()
    suspend fun updateQuantity(product: Product) = ref.child(product.id).child("quantity").setValue(product.quantity).await()
    suspend fun renameProduct(product: Product, newName: String) = ref.child(product.id).child("name").setValue(newName).await()
}
