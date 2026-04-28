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
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

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
        holder.textViewCategoria.text = categoria
        holder.frameLayout.setBackgroundResource(R.drawable.button_categoria)
        holder.itemView.setOnClickListener { onClick(categoria) }
    }

    override fun getItemCount() = categorias.size

    fun addCategoria(categoria: String) {
        if (!categorias.contains(categoria)) {
            categorias.add(0, categoria)
            notifyItemInserted(0)
        }
    }
}
