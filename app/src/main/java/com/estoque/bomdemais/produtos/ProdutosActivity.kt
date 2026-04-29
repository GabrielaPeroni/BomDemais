package com.estoque.bomdemais.produtos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.Product
import com.estoque.bomdemais.data.ShoppingItem
import com.estoque.bomdemais.utils.SwipeToDeleteCallback
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class ProdutosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProdutosAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var categoria: String
    private lateinit var fab: FloatingActionButton
    private lateinit var contextualBar: LinearLayout
    private lateinit var textSelectedCount: TextView
    private lateinit var btnContextualRename: ImageButton
    private var stopListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produtos)

        firebaseHelper = FirebaseHelper()
        categoria = intent.getStringExtra("categoria") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = categoria

        contextualBar = findViewById(R.id.contextual_bar)
        textSelectedCount = findViewById(R.id.text_selected_count)
        btnContextualRename = findViewById(R.id.btn_contextual_rename)
        fab = findViewById(R.id.fab_add_produto)

        recyclerView = findViewById(R.id.recycler_view_produtos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProdutosAdapter(
            mutableListOf(),
            onClick = {},
            onAddToList = { product -> addProductToShoppingList(product) },
            onQuantityChanged = { product -> firebaseHelper.updateProductQuantity(product) },
            onLongPress = { product ->
                adapter.enterSelectionMode(product.id)
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
                val product = adapter.getItemAt(position)
                adapter.removeProduct(product)
                Snackbar.make(recyclerView, "'${product.name}' removido", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        firebaseHelper.restoreProduct(product)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) firebaseHelper.deleteProduct(product.id)
                        }
                    })
                    .show()
            }
        )
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        fab.setOnClickListener { showAddProdutoDialog() }

        findViewById<ImageButton>(R.id.btn_contextual_close).setOnClickListener { exitSelectionMode() }

        findViewById<ImageButton>(R.id.btn_contextual_delete).setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isEmpty()) return@setOnClickListener
            MaterialAlertDialogBuilder(this)
                .setTitle("Excluir ${selected.size} produto(s)?")
                .setPositiveButton("Excluir") { _, _ ->
                    selected.forEach {
                        firebaseHelper.deleteProduct(it.id)
                        adapter.removeProduct(it)
                    }
                    exitSelectionMode()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnContextualRename.setOnClickListener {
            val product = adapter.getSelectedItems().firstOrNull() ?: return@setOnClickListener
            val input = TextInputEditText(this).apply { setText(product.name) }
            MaterialAlertDialogBuilder(this)
                .setTitle("Renomear produto")
                .setView(input)
                .setPositiveButton("Salvar") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        firebaseHelper.renameProduct(product, newName)
                        exitSelectionMode()
                        loadProductsFromFirebase()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isSelectionMode) {
                    exitSelectionMode()
                } else {
                    finish()
                }
            }
        })

        stopListener = firebaseHelper.listenToProductsByCategory(categoria) { produtos ->
            adapter.updateProducts(produtos)
        }

        setupSearchBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListener?.invoke()
        stopListener = null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showContextualBar() {
        contextualBar.visibility = View.VISIBLE
        fab.hide()
    }

    private fun exitSelectionMode() {
        adapter.exitSelectionMode()
        contextualBar.visibility = View.GONE
        fab.show()
    }

    private fun showAddProdutoDialog() {
        val input = TextInputEditText(this)
        input.hint = "Nome do Produto"

        MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Produto")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty()) {
                    addProductToDatabase(nome)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addProductToDatabase(nome: String) {
        firebaseHelper.addProduct(nome, categoria) { product ->
            val msg = if (product != null) "'$nome' adicionado!" else "Falha ao adicionar produto."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
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

    private fun addProductToShoppingList(product: Product) {
        val item = ShoppingItem(name = product.name, category = product.category, quantityToBuy = 1)
        firebaseHelper.addShoppingItem(item) { success ->
            val msg = if (success) "'${product.name}' adicionado à lista!" else "Falha ao adicionar à lista."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
