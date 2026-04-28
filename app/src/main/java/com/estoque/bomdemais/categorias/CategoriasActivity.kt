package com.estoque.bomdemais.categorias

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.produtos.ProdutosActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoriasActivity : AppCompatActivity() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var recyclerViewProdutosEmFalta: RecyclerView
    private lateinit var adapterCategorias: CategoriasAdapter
    private lateinit var adapterProdutosEmFalta: CategoriasAdapter
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        firebaseHelper = FirebaseHelper()

        findViewById<ImageView>(R.id.btn_return).setOnClickListener { finish() }

        recyclerViewCategorias = findViewById(R.id.recycler_view_categorias)
        recyclerViewCategorias.layoutManager = GridLayoutManager(this, 2)

        recyclerViewProdutosEmFalta = findViewById(R.id.recycler_view_produtos_em_falta)
        recyclerViewProdutosEmFalta.layoutManager = LinearLayoutManager(this)

        adapterCategorias = CategoriasAdapter(mutableListOf()) { categoria ->
            startActivity(Intent(this, ProdutosActivity::class.java).putExtra("categoria", categoria))
        }
        recyclerViewCategorias.adapter = adapterCategorias

        adapterProdutosEmFalta = CategoriasAdapter(mutableListOf()) { categoria ->
            startActivity(Intent(this, ProdutosActivity::class.java).putExtra("categoria", categoria))
        }
        recyclerViewProdutosEmFalta.adapter = adapterProdutosEmFalta

        findViewById<FloatingActionButton>(R.id.fab_add_categoria).setOnClickListener {
            showAddCategoriaDialog()
        }

        loadCategoriesFromFirebase()
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
            adapterProdutosEmFalta.categorias.clear()
            adapterProdutosEmFalta.categorias.add(FirebaseHelper.PRODUTOS_EM_FALTA)
            adapterProdutosEmFalta.notifyDataSetChanged()

            adapterCategorias.categorias.clear()
            adapterCategorias.categorias.addAll(categorias)
            adapterCategorias.notifyDataSetChanged()
        }
    }
}
