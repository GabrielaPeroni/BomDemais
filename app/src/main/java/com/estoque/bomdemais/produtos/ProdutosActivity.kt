package com.estoque.bomdemais.produtos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.Product
import com.estoque.bomdemais.data.ShoppingItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class ProdutosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProdutosAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var categoria: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produtos)

        firebaseHelper = FirebaseHelper()
        categoria = intent.getStringExtra("categoria") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = categoria

        recyclerView = findViewById(R.id.recycler_view_produtos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProdutosAdapter(
            mutableListOf(),
            onClick = {},
            onAddToList = { product -> addProductToShoppingList(product) },
            onQuantityChanged = { product -> firebaseHelper.updateProductQuantity(product) }
        )
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab_add_produto).setOnClickListener {
            showAddProdutoDialog()
        }

        setupSearchBar()
    }

    override fun onResume() {
        super.onResume()
        loadProductsFromFirebase()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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
            if (product != null) {
                Toast.makeText(this, "'$nome' adicionado!", Toast.LENGTH_SHORT).show()
                adapter.addProduct(product)
            } else {
                Toast.makeText(this, "Falha ao adicionar produto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProductsFromFirebase() {
        firebaseHelper.getProductsByCategory(categoria) { produtos ->
            adapter.updateProducts(produtos)
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
