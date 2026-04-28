package com.estoque.bomdemais.notas

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotasAdapter
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)

        firebaseHelper = FirebaseHelper()

        findViewById<ImageView>(R.id.btn_return).setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recycler_view_notas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NotasAdapter(mutableListOf()) { note ->
            firebaseHelper.deleteNote(note.id)
            adapter.removeNote(note)
        }
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab_add_nota).setOnClickListener {
            showAddNotaDialog()
        }

        loadNotesFromFirebase()
    }

    private fun showAddNotaDialog() {
        val input = EditText(this)
        input.hint = "Escreva sua nota..."

        MaterialAlertDialogBuilder(this)
            .setTitle("Nova Nota")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    firebaseHelper.addNote(text) { note ->
                        if (note != null) adapter.addNote(note)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadNotesFromFirebase() {
        firebaseHelper.getNotes { notes ->
            adapter.updateNotes(notes)
        }
    }
}
