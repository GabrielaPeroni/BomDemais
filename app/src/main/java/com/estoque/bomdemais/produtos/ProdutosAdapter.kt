package com.estoque.bomdemais.produtos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Product

class ProdutosAdapter(
    private var productList: MutableList<Product>,
    private val onClick: (Product) -> Unit,
    private val onAddToList: (Product) -> Unit,
    private val onQuantityChanged: (Product) -> Unit
) : RecyclerView.Adapter<ProdutosAdapter.ProdutoViewHolder>() {

    private var filteredList: MutableList<Product> = productList.toMutableList()

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_product_name)
        val quantityTextView: TextView = view.findViewById(R.id.text_quantity)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
        val categoryTextView: TextView = view.findViewById(R.id.text_product_category)
        val btnAddToList: Button = view.findViewById(R.id.btn_add_to_list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val product = filteredList[position]

        holder.nameTextView.text = product.name
        holder.quantityTextView.text = product.quantity.toString()
        holder.categoryTextView.text = product.category
        holder.btnAddToList.visibility = View.VISIBLE

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

        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount() = filteredList.size

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

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            productList.toMutableList()
        } else {
            productList.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
