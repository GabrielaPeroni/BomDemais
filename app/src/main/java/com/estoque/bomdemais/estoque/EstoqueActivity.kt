package com.estoque.bomdemais.estoque

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EstoqueActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterEstoque
    private lateinit var fabAddProduct: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estoque)

        // Return button
        val returnButton = findViewById<ImageView>(R.id.btn_return)
        returnButton.setOnClickListener {
            finish()
        }

        // Configuring RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdapterEstoque(mutableListOf())  // Initial empty list
        recyclerView.adapter = adapter

        // Configuring FloatingActionButton
        fabAddProduct = findViewById(R.id.fab_add_product)
        fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }

        // Load products from Firebase
        FirebaseHelper().getProducts { productList ->
            adapter.updateProducts(productList)
        }
    }

    private fun showAddProductDialog() {
        val dialog = AddEstoqueDialog()
        dialog.show(supportFragmentManager, "AddProductDialog")
    }
}
