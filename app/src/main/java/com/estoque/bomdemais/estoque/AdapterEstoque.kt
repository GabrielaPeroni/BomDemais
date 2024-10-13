package com.estoque.bomdemais.estoque

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.data.Product
import com.estoque.bomdemais.R

class AdapterEstoque(private var productList: MutableList<Product>) : RecyclerView.Adapter<AdapterEstoque.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_product_name)
        val categoryTextView: TextView = view.findViewById(R.id.text_product_category)
        val quantityTextView: TextView = view.findViewById(R.id.text_quantity)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.nameTextView.text = product.name
        holder.categoryTextView.text = product.category
        holder.quantityTextView.text = product.quantity.toString()

        holder.btnIncrease.setOnClickListener {
            product.quantity++
            notifyItemChanged(position)
        }

        holder.btnDecrease.setOnClickListener {
            if (product.quantity > 0) {
                product.quantity--
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount() = productList.size

    fun updateProducts(newProducts: List<Product>) {
        productList.clear()
        productList.addAll(newProducts)
        notifyDataSetChanged()
    }
}
