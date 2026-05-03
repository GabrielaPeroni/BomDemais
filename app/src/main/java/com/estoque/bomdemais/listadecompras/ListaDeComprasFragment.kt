package com.estoque.bomdemais.listadecompras

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
import com.estoque.bomdemais.data.ShoppingItem
import com.estoque.bomdemais.utils.SwipeToDeleteCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ListaDeComprasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListaDeComprasAdapter
    private val viewModel: ListaDeComprasViewModel by viewModels { ListaDeComprasViewModel.Factory }
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
                R.id.action_rename -> { handleRename(mode); true }
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
        rootView = inflater.inflate(R.layout.fragment_lista_de_compras, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = view.findViewById(R.id.fab_add_item)
        emptyState = view.findViewById(R.id.empty_state)
        recyclerView = view.findViewById(R.id.recycler_view_lista)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ListaDeComprasAdapter(
            mutableListOf(),
            onCheckedChanged = { item -> viewModel.toggleChecked(item) },
            onQuantityChanged = { item -> viewModel.updateQuantity(item) },
            onLongPress = { item ->
                adapter.enterSelectionMode(item.id)
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                fab.hide()
            },
            onSelectionChanged = { actionMode?.invalidate() }
        )
        recyclerView.adapter = adapter

        val swipeCallback = SwipeToDeleteCallback(
            isEnabled = { !adapter.isSelectionMode },
            onSwiped = { position ->
                val item = adapter.getItemAt(position)
                adapter.removeItem(item)
                Snackbar.make(rootView, "'${item.name}' removido", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        adapter.addItem(item)
                        viewModel.restoreItem(item)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) viewModel.deleteItem(item)
                        }
                    })
                    .show()
            }
        )
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        fab.setOnClickListener { showAddItemDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect {
                    adapter.updateItems(it)
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
            .setTitle("Excluir ${selected.size} item(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                selected.forEach {
                    viewModel.deleteItem(it)
                    adapter.removeItem(it)
                }
                mode.finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handleRename(mode: ActionMode) {
        val item = adapter.getSelectedItems().firstOrNull() ?: return
        val input = TextInputEditText(requireContext()).apply { setText(item.name) }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Renomear item")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.renameItem(item, newName)
                    mode.finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddItemDialog() {
        val padding = (20 * resources.displayMetrics.density).toInt()
        val input = com.google.android.material.textfield.TextInputEditText(requireContext()).apply {
            hint = "Nome do item"
        }
        val container = android.widget.FrameLayout(requireContext()).apply {
            setPadding(padding, 0, padding, 0)
            addView(input)
        }
        val dialog = MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Adicionar à lista")
            .setView(container)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) viewModel.addItem(name)
            }
            .setNegativeButton("Cancelar", null)
            .show()
        input.requestFocus()
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}
