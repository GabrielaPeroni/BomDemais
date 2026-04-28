package com.estoque.bomdemais.produtos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.Product
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProdutosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProdutosAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var categoria: String
    private val zeroQuantityProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produtos)

        firebaseHelper = FirebaseHelper()
        categoria = intent.getStringExtra("categoria") ?: ""

        findViewById<ImageView>(R.id.btn_return).setOnClickListener {
            if (categoria != FirebaseHelper.PRODUTOS_EM_FALTA) {
                showConfirmationDialog()
            } else {
                finish()
            }
        }

        recyclerView = findViewById(R.id.recycler_view_produtos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProdutosAdapter(
            mutableListOf(),
            onClick = { produto ->
                Toast.makeText(this, "Produto selecionado: ${produto.name}", Toast.LENGTH_SHORT).show()
            },
            onZeroQuantity = { product -> addZeroQuantityProduct(product) }
        )
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.top_bar_text).text = categoria

        findViewById<FloatingActionButton>(R.id.fab_add_produto).setOnClickListener {
            showAddProdutoDialog()
        }

        loadProductsFromFirebase()
        setupSearchBar()
    }

    private fun showAddProdutoDialog() {
        val input = EditText(this)
        input.hint = "Nome do Produto"

        MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Produto")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val produto = input.text.toString().trim()
                if (produto.isNotEmpty()) {
                    addProdutoToDatabase(produto)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addProdutoToDatabase(produto: String) {
        firebaseHelper.addProduct(produto, categoria)
        Toast.makeText(this, "'$produto' adicionado!", Toast.LENGTH_SHORT).show()
        adapter.addProduto(produto)
    }

    private fun loadProductsFromFirebase() {
        firebaseHelper.getProductsByCategory(categoria) { produtos ->
            adapter.updateProducts(produtos)
        }
    }

    private fun setupSearchBar() {
        findViewById<EditText>(R.id.search_bar).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filtrar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showConfirmationDialog() {
        if (zeroQuantityProducts.isNotEmpty()) {
            val productNames = zeroQuantityProducts.joinToString("\n") { it.name }
            MaterialAlertDialogBuilder(this)
                .setTitle("Mover Produtos")
                .setMessage("Os seguintes produtos estao zerados:\n\n$productNames\n\nDeseja movê-los para '${FirebaseHelper.PRODUTOS_EM_FALTA}'?")
                .setPositiveButton("Confirmar") { _, _ -> moveProductsToLack(zeroQuantityProducts) }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            finish()
        }
    }

    private fun addZeroQuantityProduct(product: Product) {
        if (!zeroQuantityProducts.contains(product)) {
            zeroQuantityProducts.add(product)
        }
    }

    private fun moveProductsToLack(products: List<Product>) {
        products.forEach { product ->
            firebaseHelper.moveProductToLackCategory(product)
            adapter.removeProduct(product)
        }
        zeroQuantityProducts.clear()
        Toast.makeText(this, "Produtos movidos com sucesso", Toast.LENGTH_SHORT).show()
        finish()
    }
}
