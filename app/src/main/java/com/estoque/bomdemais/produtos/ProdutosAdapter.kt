package com.estoque.bomdemais.produtos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Product
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class ProdutosAdapter(
    private var productList: MutableList<Product>,
    private val onClick: (Product) -> Unit,
    private val onAddToList: (Product) -> Unit,
    private val onQuantityChanged: (Product) -> Unit,
    private val onLongPress: (Product) -> Unit,
    private val onSelectionChanged: (count: Int) -> Unit
) : RecyclerView.Adapter<ProdutosAdapter.ProdutoViewHolder>() {

    private var filteredList: MutableList<Product> = productList.toMutableList()

    var isSelectionMode = false
        private set
    private val selectedIds = mutableSetOf<String>()

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_product_name)
        val unitTextView: TextView = view.findViewById(R.id.text_product_unit)
        val quantityTextView: TextView = view.findViewById(R.id.text_quantity)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
        val btnAddToList: Button = view.findViewById(R.id.btn_add_to_list)
        val stepperLayout: View = view.findViewById(R.id.stepper_layout)
        val badgeLowStock: TextView = view.findViewById(R.id.badge_low_stock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val product = filteredList[position]
        val isSelected = selectedIds.contains(product.id)

        holder.nameTextView.text = product.name
        holder.unitTextView.text = product.unit
        holder.quantityTextView.text = product.quantity.toString()

        holder.badgeLowStock.visibility =
            if (!isSelectionMode && product.quantity < product.minQuantity) View.VISIBLE else View.GONE

        holder.stepperLayout.visibility = if (isSelectionMode) View.GONE else View.VISIBLE
        holder.btnAddToList.visibility = if (isSelectionMode) View.GONE else View.VISIBLE

        (holder.itemView as MaterialCardView).setCardBackgroundColor(
            if (isSelected) ContextCompat.getColor(holder.itemView.context, R.color.card_selected_bg)
            else MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSurface)
        )

        holder.itemView.setOnClickListener {
            if (isSelectionMode) toggleSelection(product.id)
            else onClick(product)
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) onLongPress(product)
            true
        }

        holder.btnAddToList.setOnClickListener { onAddToList(product) }

        holder.btnIncrease.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            product.quantity++
            onQuantityChanged(product)
            notifyItemChanged(pos)
        }

        holder.btnDecrease.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            if (product.quantity > 0) {
                product.quantity--
                onQuantityChanged(product)
                notifyItemChanged(pos)
            }
        }
    }

    override fun getItemCount() = filteredList.size

    fun getItemAt(position: Int): Product = filteredList[position]

    fun removeProduct(product: Product) {
        productList.remove(product)
        val position = filteredList.indexOf(product)
        if (position >= 0) {
            filteredList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateProducts(newProducts: List<Product>) {
        productList.clear()
        productList.addAll(newProducts)
        filteredList = productList.toMutableList()
        notifyDataSetChanged()
    }

    fun addProduct(product: Product) {
        productList.add(0, product)
        filteredList.add(0, product)
        notifyItemInserted(0)
    }

    fun restoreProduct(product: Product) = addProduct(product)

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            productList.toMutableList()
        } else {
            productList.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun enterSelectionMode(id: String) {
        isSelectionMode = true
        selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Product> = filteredList.filter { selectedIds.contains(it.id) }

    private fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }
}
