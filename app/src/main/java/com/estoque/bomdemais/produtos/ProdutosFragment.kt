package com.estoque.bomdemais.produtos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ProdutosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProdutosAdapter
    private lateinit var categoria: String
    private lateinit var fab: FloatingActionButton
    private var actionMode: ActionMode? = null

    private val viewModel: ProdutosViewModel by viewModels {
        ProdutosViewModel.factory(categoria)
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoria = arguments?.getString(ARG_CATEGORIA) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_produtos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = categoria
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        fab = view.findViewById(R.id.fab_add_produto)
        recyclerView = view.findViewById(R.id.recycler_view_produtos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProdutosAdapter(
            mutableListOf(),
            onClick = {},
            onAddToList = { product ->
                viewModel.addToShoppingList(product)
                Toast.makeText(requireContext(), "'${product.name}' adicionado à lista!", Toast.LENGTH_SHORT).show()
            },
            onQuantityChanged = { product -> viewModel.updateQuantity(product) },
            onLongPress = { product ->
                adapter.enterSelectionMode(product.id)
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                fab.hide()
            },
            onSelectionChanged = { actionMode?.invalidate() }
        )
        recyclerView.adapter = adapter

        val swipeCallback = SwipeToDeleteCallback(
            isEnabled = { !adapter.isSelectionMode },
            onSwiped = { position ->
                val product = adapter.getItemAt(position)
                adapter.removeProduct(product)
                Snackbar.make(recyclerView, "'${product.name}' removido", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        adapter.addProduct(product)
                        viewModel.restoreProduct(product)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) viewModel.deleteProduct(product)
                        }
                    })
                    .show()
            }
        )
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        fab.setOnClickListener { showAddProdutoDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { adapter.updateProducts(it) }
            }
        }

        setupSearchBar(view)
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
            .setTitle("Excluir ${selected.size} produto(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                selected.forEach {
                    viewModel.deleteProduct(it)
                    adapter.removeProduct(it)
                }
                mode.finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handleRename(mode: ActionMode) {
        val product = adapter.getSelectedItems().firstOrNull() ?: return
        val input = TextInputEditText(requireContext()).apply { setText(product.name) }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Renomear produto")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.renameProduct(product, newName)
                    mode.finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddProdutoDialog() {
        val input = TextInputEditText(requireContext()).apply { hint = "Nome do Produto" }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Adicionar Produto")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty()) viewModel.addProduct(nome, categoria)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupSearchBar(view: View) {
        view.findViewById<TextInputEditText>(R.id.search_bar).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { adapter.filter(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    companion object {
        private const val ARG_CATEGORIA = "categoria"

        fun newInstance(categoria: String) = ProdutosFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORIA, categoria) }
        }
    }
}
