package com.estoque.bomdemais.categorias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Category
import com.estoque.bomdemais.produtos.ProdutosFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CategoriasFragment : Fragment() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var adapterCategorias: CategoriasAdapter
    private val viewModel: CategoriasViewModel by viewModels { CategoriasViewModel.Factory }
    private lateinit var fab: FloatingActionButton
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.contextual_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            val count = adapterCategorias.getSelectedItems().size
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
            adapterCategorias.exitSelectionMode()
            fab.show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_categorias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = view.findViewById(R.id.fab_add_categoria)
        recyclerViewCategorias = view.findViewById(R.id.recycler_view_categorias)
        recyclerViewCategorias.layoutManager = GridLayoutManager(requireContext(), 2)

        adapterCategorias = CategoriasAdapter(
            mutableListOf(),
            onClick = { category ->
                parentFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, ProdutosFragment.newInstance(category.name))
                    .addToBackStack(null)
                    .commit()
            },
            onLongPress = { category ->
                adapterCategorias.enterSelectionMode(category)
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                fab.hide()
            },
            onSelectionChanged = { actionMode?.invalidate() }
        )
        recyclerViewCategorias.adapter = adapterCategorias

        fab.setOnClickListener { showAddCategoriaDialog() }

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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) { actionMode?.finish(); actionMode = null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode?.finish()
        actionMode = null
    }

    private fun handleDelete(mode: ActionMode) {
        val selected = adapterCategorias.getSelectedItems()
        if (selected.isEmpty()) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir ${selected.size} categoria(s)?")
            .setMessage("Todos os produtos dentro dessas categorias também serão excluídos.")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteCategories(selected)
                selected.forEach { adapterCategorias.removeCategoria(it) }
                mode.finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handleRename(mode: ActionMode) {
        val category = adapterCategorias.getSelectedItems().firstOrNull() ?: return
        val input = TextInputEditText(requireContext()).apply { setText(category.name) }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Renomear categoria")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != category.name) {
                    viewModel.renameCategory(category, newName) { success ->
                        if (!success) Toast.makeText(requireContext(), "Falha ao renomear.", Toast.LENGTH_SHORT).show()
                    }
                    mode.finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddCategoriaDialog() {
        val input = EditText(requireContext()).apply { hint = "Nome da Categoria" }
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Adicionar Categoria")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val categoria = input.text.toString().trim()
                if (categoria.isNotEmpty()) viewModel.addCategory(categoria)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
