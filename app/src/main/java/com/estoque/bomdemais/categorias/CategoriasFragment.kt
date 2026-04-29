package com.estoque.bomdemais.categorias

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.produtos.ProdutosActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriasFragment : Fragment() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var adapterCategorias: CategoriasAdapter
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_categorias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Estoque"

        firebaseHelper = FirebaseHelper()

        recyclerViewCategorias = view.findViewById(R.id.recycler_view_categorias)
        recyclerViewCategorias.layoutManager = GridLayoutManager(requireContext(), 2)

        adapterCategorias = CategoriasAdapter(mutableListOf()) { categoria ->
            startActivity(Intent(requireContext(), ProdutosActivity::class.java).putExtra("categoria", categoria))
        }
        recyclerViewCategorias.adapter = adapterCategorias

        view.findViewById<FloatingActionButton>(R.id.fab_add_categoria).setOnClickListener {
            showAddCategoriaDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategoriesFromFirebase()
    }

    private fun showAddCategoriaDialog() {
        val input = EditText(requireContext())
        input.hint = "Nome da Categoria"

        MaterialAlertDialogBuilder(requireActivity())
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
                Toast.makeText(requireContext(), "Categoria '$categoria' adicionada!", Toast.LENGTH_SHORT).show()
                adapterCategorias.addCategoria(categoria)
            } else {
                Toast.makeText(requireContext(), "Falha ao adicionar categoria.", Toast.LENGTH_SHORT).show()
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
