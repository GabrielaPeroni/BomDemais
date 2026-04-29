package com.estoque.bomdemais.notas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.utils.SwipeToDeleteCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class NotasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotasAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var fab: FloatingActionButton
    private lateinit var contextualBar: LinearLayout
    private lateinit var textSelectedCount: TextView
    private lateinit var btnContextualRename: ImageButton
    private lateinit var rootView: View
    private var stopListener: (() -> Unit)? = null

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            exitSelectionMode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_notas, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Notas"

        firebaseHelper = FirebaseHelper()

        contextualBar = view.findViewById(R.id.contextual_bar)
        textSelectedCount = view.findViewById(R.id.text_selected_count)
        btnContextualRename = view.findViewById(R.id.btn_contextual_rename)
        fab = view.findViewById(R.id.fab_add_nota)

        recyclerView = view.findViewById(R.id.recycler_view_notas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotasAdapter(
            mutableListOf(),
            onLongPress = { note ->
                adapter.enterSelectionMode(note.id)
                showContextualBar()
            },
            onSelectionChanged = { count ->
                textSelectedCount.text = "$count selecionado(s)"
                btnContextualRename.visibility = if (count == 1) View.VISIBLE else View.GONE
            }
        )
        recyclerView.adapter = adapter

        val swipeCallback = SwipeToDeleteCallback(
            isEnabled = { !adapter.isSelectionMode },
            onSwiped = { position ->
                val note = adapter.getItemAt(position)
                adapter.removeNote(note)
                Snackbar.make(rootView, "Nota removida", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        firebaseHelper.restoreNote(note)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) firebaseHelper.deleteNote(note.id)
                        }
                    })
                    .show()
            }
        )
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        fab.setOnClickListener { showAddNotaDialog() }

        view.findViewById<ImageButton>(R.id.btn_contextual_close).setOnClickListener { exitSelectionMode() }

        view.findViewById<ImageButton>(R.id.btn_contextual_delete).setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isEmpty()) return@setOnClickListener
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir ${selected.size} nota(s)?")
                .setPositiveButton("Excluir") { _, _ ->
                    selected.forEach {
                        firebaseHelper.deleteNote(it.id)
                        adapter.removeNote(it)
                    }
                    exitSelectionMode()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnContextualRename.setOnClickListener {
            val note = adapter.getSelectedItems().firstOrNull() ?: return@setOnClickListener
            val input = TextInputEditText(requireContext()).apply { setText(note.text) }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editar nota")
                .setView(input)
                .setPositiveButton("Salvar") { _, _ ->
                    val newText = input.text.toString().trim()
                    if (newText.isNotEmpty()) {
                        firebaseHelper.renameNote(note, newText)
                        exitSelectionMode()
                        loadNotesFromFirebase()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        stopListener = firebaseHelper.listenToNotes { notes -> adapter.updateNotes(notes) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopListener?.invoke()
        stopListener = null
    }

    private fun showContextualBar() {
        contextualBar.visibility = View.VISIBLE
        fab.hide()
        backPressedCallback.isEnabled = true
    }

    private fun exitSelectionMode() {
        adapter.exitSelectionMode()
        contextualBar.visibility = View.GONE
        fab.show()
        backPressedCallback.isEnabled = false
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
                    firebaseHelper.addNote(text) { }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}
