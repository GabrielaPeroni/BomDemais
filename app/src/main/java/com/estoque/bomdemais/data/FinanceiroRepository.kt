package com.estoque.bomdemais.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceiroRepository {
    private val ref = FirebaseDatabase.getInstance().getReference("transacoes")
        .also { it.keepSynced(true) }

    fun transactionsByMonth(monthKey: String): Flow<List<Transaction>> = callbackFlow {
        val query = ref.orderByChild("monthKey").equalTo(monthKey)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val transactions = snap.children
                    .mapNotNull { it.getValue(Transaction::class.java) }
                    .sortedByDescending { it.date }
                trySend(transactions)
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addTransaction(type: String, amount: Double, description: String, date: Long): Transaction? {
        val key = ref.push().key ?: return null
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(date))
        val transaction = Transaction(
            id = key,
            type = type,
            amount = amount,
            description = description,
            date = date,
            monthKey = monthKey
        )
        return try { ref.child(key).setValue(transaction).await(); transaction }
        catch (e: Exception) { null }
    }

    suspend fun deleteTransaction(id: String) = ref.child(id).removeValue().await()
    suspend fun restoreTransaction(transaction: Transaction) = ref.child(transaction.id).setValue(transaction).await()

    suspend fun editTransaction(transaction: Transaction, type: String, amount: Double, description: String, date: Long) {
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(date))
        ref.child(transaction.id).updateChildren(
            mapOf("type" to type, "amount" to amount, "description" to description, "date" to date, "monthKey" to monthKey)
        ).await()
    }
}
