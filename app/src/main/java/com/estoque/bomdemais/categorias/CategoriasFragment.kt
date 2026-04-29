package com.estoque.bomdemais.categorias

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.produtos.ProdutosActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CategoriasFragment : Fragment() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var adapterCategorias: CategoriasAdapter
    private val viewModel: CategoriasViewModel by viewModels { CategoriasViewModel.Factory }
    private lateinit var fab: FloatingActionButton
    private lateinit var contextualBar: LinearLayout
    private lateinit var textSelectedCount: TextView
    private lateinit var btnContextualRename: ImageButton

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            exitSelectionMode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_categorias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Estoque"

        contextualBar = view.findViewById(R.id.contextual_bar)
        textSelectedCount = view.findViewById(R.id.text_selected_count)
        btnContextualRename = view.findViewById(R.id.btn_contextual_rename)
        fab = view.findViewById(R.id.fab_add_categoria)

        recyclerViewCategorias = view.findViewById(R.id.recycler_view_categorias)
        recyclerViewCategorias.layoutManager = GridLayoutManager(requireContext(), 2)

        adapterCategorias = CategoriasAdapter(
            mutableListOf(),
            onClick = { categoria ->
                startActivity(Intent(requireContext(), ProdutosActivity::class.java).putExtra("categoria", categoria))
            },
            onLongPress = { categoria ->
                adapterCategorias.enterSelectionMode(categoria)
                showContextualBar()
            },
            onSelectionChanged = { count ->
                textSelectedCount.text = "$count selecionado(s)"
                btnContextualRename.visibility = if (count == 1) View.VISIBLE else View.GONE
            }
        )
        recyclerViewCategorias.adapter = adapterCategorias

        fab.setOnClickListener { showAddCategoriaDialog() }

        view.findViewById<ImageButton>(R.id.btn_contextual_close).setOnClickListener { exitSelectionMode() }

        view.findViewById<ImageButton>(R.id.btn_contextual_delete).setOnClickListener {
            val selected = adapterCategorias.getSelectedItems()
            if (selected.isEmpty()) return@setOnClickListener
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir ${selected.size} categoria(s)?")
                .setMessage("Todos os produtos dentro dessas categorias também serão excluídos.")
                .setPositiveButton("Excluir") { _, _ ->
                    viewModel.deleteCategories(selected)
                    selected.forEach { adapterCategorias.removeCategoria(it) }
                    exitSelectionMode()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnContextualRename.setOnClickListener {
            val oldName = adapterCategorias.getSelectedItems().firstOrNull() ?: return@setOnClickListener
            val input = TextInputEditText(requireContext()).apply { setText(oldName) }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Renomear categoria")
                .setView(input)
                .setPositiveButton("Salvar") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty() && newName != oldName) {
                        viewModel.renameCategory(oldName, newName) { success ->
                            if (!success) Toast.makeText(requireContext(), "Falha ao renomear.", Toast.LENGTH_SHORT).show()
                        }
                        exitSelectionMode()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { cats ->
                    adapterCategorias.categorias.clear()
                    adapterCategorias.categorias.addAll(cats)
                    adapterCategorias.notifyDataSetChanged()
                }
            }
        }
    }

    private fun showContextualBar() {
        contextualBar.visibility = View.VISIBLE
        fab.hide()
        backPressedCallback.isEnabled = true
    }

    private fun exitSelectionMode() {
        adapterCategorias.exitSelectionMode()
        contextualBar.visibility = View.GONE
        fab.show()
        backPressedCallback.isEnabled = false
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
                    viewModel.addCategory(categoria)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
