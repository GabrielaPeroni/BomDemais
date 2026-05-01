package com.estoque.bomdemais.produtos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.activity.viewModels
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

class ProdutosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProdutosAdapter
    private lateinit var categoria: String
    private lateinit var fab: FloatingActionButton
    private var actionMode: ActionMode? = null

    private val viewModel: ProdutosViewModel by viewModels {
        ProdutosViewModel.factory(intent.getStringExtra("categoria") ?: "")
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
        setContentView(R.layout.activity_produtos)

        categoria = intent.getStringExtra("categoria") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = categoria

        fab = findViewById(R.id.fab_add_produto)
        recyclerView = findViewById(R.id.recycler_view_produtos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProdutosAdapter(
            mutableListOf(),
            onClick = {},
            onAddToList = { product ->
                viewModel.addToShoppingList(product.name, product.category)
                Toast.makeText(this, "'${product.name}' adicionado à lista!", Toast.LENGTH_SHORT).show()
            },
            onQuantityChanged = { product -> viewModel.updateQuantity(product) },
            onLongPress = { product ->
                adapter.enterSelectionMode(product.id)
                actionMode = startSupportActionMode(actionModeCallback)
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { adapter.updateProducts(it) }
            }
        }

        setupSearchBar()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun handleDelete(mode: ActionMode) {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        MaterialAlertDialogBuilder(this)
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
        val input = TextInputEditText(this).apply { setText(product.name) }
        MaterialAlertDialogBuilder(this)
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
        val input = TextInputEditText(this).apply { hint = "Nome do Produto" }
        MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Produto")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty()) viewModel.addProduct(nome, categoria)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupSearchBar() {
        findViewById<TextInputEditText>(R.id.search_bar).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
