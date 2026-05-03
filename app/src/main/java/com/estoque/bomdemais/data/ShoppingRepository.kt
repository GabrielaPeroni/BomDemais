package com.estoque.bomdemais.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ShoppingRepository {
    private val ref = FirebaseDatabase.getInstance().getReference("lista_compras")
        .also { it.keepSynced(true) }

    fun shoppingItems(): Flow<List<ShoppingItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val items = snap.children.mapNotNull { it.getValue(ShoppingItem::class.java) }
                trySend(items)
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addItem(name: String, unit: String = "un") {
        val key = ref.push().key ?: return
        ref.child(key).setValue(ShoppingItem(id = key, name = name, unit = unit)).await()
    }

    suspend fun addOrIncrementItem(name: String, unit: String = "un", amount: Int = 1) {
        val snapshot = ref.orderByChild("name").equalTo(name).get().await()
        val existing = snapshot.children.firstOrNull()
        if (existing != null) {
            val current = existing.getValue(ShoppingItem::class.java) ?: return
            ref.child(current.id).child("quantity").setValue(current.quantity + amount).await()
        } else {
            val key = ref.push().key ?: return
            ref.child(key).setValue(ShoppingItem(id = key, name = name, quantity = amount, unit = unit)).await()
        }
    }

    suspend fun deleteItem(id: String) = ref.child(id).removeValue().await()
    suspend fun restoreItem(item: ShoppingItem) = ref.child(item.id).setValue(item).await()
    suspend fun updateQuantity(item: ShoppingItem) = ref.child(item.id).child("quantity").setValue(item.quantity).await()
    suspend fun updateChecked(item: ShoppingItem) = ref.child(item.id).child("isChecked").setValue(item.isChecked).await()
    suspend fun renameItem(item: ShoppingItem, newName: String) = ref.child(item.id).child("name").setValue(newName).await()
}
