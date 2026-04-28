package com.estoque.bomdemais.categorias

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.produtos.ProdutosActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriasActivity : AppCompatActivity() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var adapterCategorias: CategoriasAdapter
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        firebaseHelper = FirebaseHelper()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerViewCategorias = findViewById(R.id.recycler_view_categorias)
        recyclerViewCategorias.layoutManager = GridLayoutManager(this, 2)

        adapterCategorias = CategoriasAdapter(mutableListOf()) { categoria ->
            startActivity(Intent(this, ProdutosActivity::class.java).putExtra("categoria", categoria))
        }
        recyclerViewCategorias.adapter = adapterCategorias

        findViewById<FloatingActionButton>(R.id.fab_add_categoria).setOnClickListener {
            showAddCategoriaDialog()
        }

        loadCategoriesFromFirebase()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showAddCategoriaDialog() {
        val input = EditText(this)
        input.hint = "Nome da Categoria"

        MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Categoria")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val categoria = input.text.toString().trim()
                if (categoria.isNotEmpty()) {
                    addCategoriaToDatabase(categoria)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addCategoriaToDatabase(categoria: String) {
        firebaseHelper.addCategoria(categoria) { success ->
            if (success) {
                Toast.makeText(this, "Categoria '$categoria' adicionada!", Toast.LENGTH_SHORT).show()
                adapterCategorias.addCategoria(categoria)
            } else {
                Toast.makeText(this, "Falha ao adicionar categoria.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategoriesFromFirebase() {
        firebaseHelper.getCategorias { categorias ->
            adapterCategorias.categorias.clear()
            adapterCategorias.categorias.addAll(categorias)
            adapterCategorias.notifyDataSetChanged()
        }
    }
}
