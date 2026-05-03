package com.estoque.bomdemais.notas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.utils.SwipeToDeleteCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class NotasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotasAdapter
    private val viewModel: NotasViewModel by viewModels { NotasViewModel.Factory }
    private lateinit var fab: FloatingActionButton
    private lateinit var rootView: View
    private lateinit var emptyState: View
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.contextual_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            val count = adapter.getSelectedItems().size
            mode.title = "$count selecionado(s)"
            menu.findItem(R.id.action_rename)?.isVisible = (count == 1)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_delete -> { handleDelete(mode); true }
                R.id.action_rename -> { handleEdit(mode); true }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.exitSelectionMode()
            fab.show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_notas, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = view.findViewById(R.id.fab_add_nota)
        emptyState = view.findViewById(R.id.empty_state)
        recyclerView = view.findViewById(R.id.recycler_view_notas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotasAdapter(
            mutableListOf(),
            onTap = { note ->
                parentFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, NoteEditorFragment.newInstance(note))
                    .addToBackStack(null)
                    .commit()
            },
            onLongPress = { note ->
                adapter.enterSelectionMode(note.id)
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                fab.hide()
            },
            onSelectionChanged = { actionMode?.invalidate() }
        )
        recyclerView.adapter = adapter

        val swipeCallback = SwipeToDeleteCallback(
            isEnabled = { !adapter.isSelectionMode },
            onSwiped = { position ->
                val note = adapter.getItemAt(position)
                adapter.removeNote(note)
                Snackbar.make(rootView, "Nota removida", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        adapter.addNote(note)
                        viewModel.restoreNote(note)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) viewModel.deleteNote(note)
                        }
                    })
                    .show()
            }
        )
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        fab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.fragment_container, NoteEditorFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notes.collect {
                    adapter.updateNotes(it)
                    emptyState.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) { actionMode?.finish(); actionMode = null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode?.finish()
        actionMode = null
    }

    private fun handleDelete(mode: ActionMode) {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir ${selected.size} nota(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                selected.forEach {
                    viewModel.deleteNote(it)
                    adapter.removeNote(it)
                }
                mode.finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handleEdit(mode: ActionMode) {
        val note = adapter.getSelectedItems().firstOrNull() ?: return
        mode.finish()
        parentFragmentManager.beginTransaction()
            .add(R.id.fragment_container, NoteEditorFragment.newInstance(note))
            .addToBackStack(null)
            .commit()
    }
}
