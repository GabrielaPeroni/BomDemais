package com.estoque.bomdemais.categorias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Category

class CategoriasAdapter(
    var categorias: MutableList<Category>,
    private val onClick: (Category) -> Unit,
    private val onLongPress: (Category) -> Unit,
    private val onSelectionChanged: (count: Int) -> Unit
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

    var isSelectionMode = false
        private set
    private val selectedIds = mutableSetOf<String>()

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
        val isSelected = selectedIds.contains(categoria.id)

        holder.textViewCategoria.text = categoria.name
        holder.frameLayout.setBackgroundResource(
            if (isSelected) R.drawable.button_categoria_selected else R.drawable.button_categoria
        )
        holder.textViewCategoria.setTextColor(
            if (isSelected) 0xFF28003F.toInt() else 0xFFFFFFFF.toInt()
        )

        holder.itemView.setOnClickListener {
            if (isSelectionMode) toggleSelection(categoria.id)
            else onClick(categoria)
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) onLongPress(categoria)
            true
        }
    }

    override fun getItemCount() = categorias.size

    fun removeCategoria(category: Category) {
        val position = categorias.indexOfFirst { it.id == category.id }
        if (position >= 0) {
            categorias.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun enterSelectionMode(category: Category) {
        isSelectionMode = true
        selectedIds.add(category.id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Category> = categorias.filter { selectedIds.contains(it.id) }

    private fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }
}
