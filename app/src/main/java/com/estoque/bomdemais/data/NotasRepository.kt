package com.estoque.bomdemais.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotasRepository {
    private val ref = FirebaseDatabase.getInstance().getReference("notas")
        .also { it.keepSynced(true) }

    fun notes(): Flow<List<Note>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val notes = snap.children.mapNotNull { it.getValue(Note::class.java) }
                    .sortedByDescending { it.timestamp }
                trySend(notes)
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addNote(text: String) {
        val key = ref.push().key ?: return
        ref.child(key).setValue(Note(id = key, text = text, timestamp = System.currentTimeMillis())).await()
    }

    suspend fun deleteNote(id: String) = ref.child(id).removeValue().await()
    suspend fun restoreNote(note: Note) = ref.child(note.id).setValue(note).await()
    suspend fun editNote(note: Note, newText: String) = ref.child(note.id).child("text").setValue(newText).await()
}
