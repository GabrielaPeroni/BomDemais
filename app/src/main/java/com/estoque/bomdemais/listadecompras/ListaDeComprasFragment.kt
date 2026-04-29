package com.estoque.bomdemais.listadecompras

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
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ListaDeComprasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListaDeComprasAdapter
    private val viewModel: ListaDeComprasViewModel by viewModels { ListaDeComprasViewModel.Factory }
    private lateinit var fab: FloatingActionButton
    private lateinit var contextualBar: LinearLayout
    private lateinit var textSelectedCount: TextView
    private lateinit var btnContextualRename: ImageButton
    private lateinit var rootView: View

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            exitSelectionMode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_lista_de_compras, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Lista de Compras"

        contextualBar = view.findViewById(R.id.contextual_bar)
        textSelectedCount = view.findViewById(R.id.text_selected_count)
        btnContextualRename = view.findViewById(R.id.btn_contextual_rename)
        fab = view.findViewById(R.id.fab_add_item)

        recyclerView = view.findViewById(R.id.recycler_view_lista)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ListaDeComprasAdapter(
            mutableListOf(),
            onCheckedChanged = { item -> viewModel.toggleChecked(item) },
            onQuantityChanged = { item -> viewModel.updateQuantity(item) },
            onLongPress = { item ->
                adapter.enterSelectionMode(item.id)
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
                val item = adapter.getItemAt(position)
                adapter.removeItem(item)
                Snackbar.make(rootView, "'${item.name}' removido", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
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

        view.findViewById<ImageButton>(R.id.btn_contextual_close).setOnClickListener { exitSelectionMode() }

        view.findViewById<ImageButton>(R.id.btn_contextual_delete).setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isEmpty()) return@setOnClickListener
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir ${selected.size} item(s)?")
                .setPositiveButton("Excluir") { _, _ ->
                    selected.forEach {
                        viewModel.deleteItem(it)
                        adapter.removeItem(it)
                    }
                    exitSelectionMode()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnContextualRename.setOnClickListener {
            val item = adapter.getSelectedItems().firstOrNull() ?: return@setOnClickListener
            val input = TextInputEditText(requireContext()).apply { setText(item.name) }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Renomear item")
                .setView(input)
                .setPositiveButton("Salvar") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        viewModel.renameItem(item, newName)
                        exitSelectionMode()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { adapter.updateItems(it) }
            }
        }
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

    private fun showAddItemDialog() {
        val input = EditText(requireContext())
        input.hint = "Nome do item"

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Adicionar à lista")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addItem(name)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
