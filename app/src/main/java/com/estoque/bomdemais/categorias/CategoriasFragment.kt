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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.produtos.ProdutosActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class CategoriasFragment : Fragment() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var adapterCategorias: CategoriasAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var fab: FloatingActionButton
    private lateinit var contextualBar: LinearLayout
    private lateinit var textSelectedCount: TextView
    private lateinit var btnContextualRename: ImageButton
    private var stopListener: (() -> Unit)? = null

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

        firebaseHelper = FirebaseHelper()

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
                    selected.forEach { name ->
                        firebaseHelper.deleteCategory(name)
                        adapterCategorias.removeCategoria(name)
                    }
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
                        firebaseHelper.renameCategory(oldName, newName) { success ->
                            if (success) {
                                adapterCategorias.renameCategoria(oldName, newName)
                            } else {
                                Toast.makeText(requireContext(), "Falha ao renomear.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        exitSelectionMode()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        stopListener = firebaseHelper.listenToCategories { categorias ->
            adapterCategorias.categorias.clear()
            adapterCategorias.categorias.addAll(categorias)
            adapterCategorias.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopListener?.invoke()
        stopListener = null
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

}
