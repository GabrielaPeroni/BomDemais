package com.estoque.bomdemais.notas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotasAdapter
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_notas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Notas"

        firebaseHelper = FirebaseHelper()

        recyclerView = view.findViewById(R.id.recycler_view_notas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotasAdapter(mutableListOf()) { note ->
            firebaseHelper.deleteNote(note.id)
            adapter.removeNote(note)
        }
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab_add_nota).setOnClickListener {
            showAddNotaDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotesFromFirebase()
    }

    private fun showAddNotaDialog() {
        val input = EditText(requireContext())
        input.hint = "Escreva sua nota..."

        MaterialAlertDialogBuilder(requireActivity())
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
