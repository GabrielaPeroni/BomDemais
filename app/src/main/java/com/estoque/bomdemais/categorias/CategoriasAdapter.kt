package com.estoque.bomdemais.categorias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R

class CategoriasAdapter(
    var categorias: MutableList<String>,
    private val onClick: (String) -> Unit,
    private val onLongPress: (String) -> Unit,
    private val onSelectionChanged: (count: Int) -> Unit
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

    var isSelectionMode = false
        private set
    private val selectedNames = mutableSetOf<String>()

    class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val frameLayout: FrameLayout = view.findViewById(R.id.frame_layout)
        val textViewCategoria: TextView = view.findViewById(R.id.text_categoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        val isSelected = selectedNames.contains(categoria)

        holder.textViewCategoria.text = categoria
        holder.frameLayout.setBackgroundResource(
            if (isSelected) R.drawable.button_categoria_selected else R.drawable.button_categoria
        )

        holder.itemView.setOnClickListener {
            if (isSelectionMode) toggleSelection(categoria)
            else onClick(categoria)
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) onLongPress(categoria)
            true
        }
    }

    override fun getItemCount() = categorias.size

    fun addCategoria(categoria: String) {
        if (!categorias.contains(categoria)) {
            categorias.add(0, categoria)
            notifyItemInserted(0)
        }
    }

    fun removeCategoria(name: String) {
        val position = categorias.indexOf(name)
        if (position >= 0) {
            categorias.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun renameCategoria(oldName: String, newName: String) {
        val position = categorias.indexOf(oldName)
        if (position >= 0) {
            categorias[position] = newName
            notifyItemChanged(position)
        }
    }

    fun enterSelectionMode(name: String) {
        isSelectionMode = true
        selectedNames.add(name)
        notifyDataSetChanged()
        onSelectionChanged(selectedNames.size)
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedNames.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<String> = categorias.filter { selectedNames.contains(it) }

    private fun toggleSelection(name: String) {
        if (selectedNames.contains(name)) selectedNames.remove(name) else selectedNames.add(name)
        notifyDataSetChanged()
        onSelectionChanged(selectedNames.size)
    }
}
