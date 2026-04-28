package com.estoque.bomdemais.produtos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.Product

class ProdutosAdapter(
    private var productList: MutableList<Product>,
    private val onClick: (Product) -> Unit,
    private val onZeroQuantity: (Product) -> Unit
) : RecyclerView.Adapter<ProdutosAdapter.ProdutoViewHolder>() {

    private var productListFiltrada: MutableList<Product> = productList.toMutableList()

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_product_name)
        val quantityTextView: TextView = view.findViewById(R.id.text_quantity)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
        val categoryTextView: TextView = view.findViewById(R.id.text_product_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val product = productListFiltrada[position]

        holder.nameTextView.text = product.name
        holder.quantityTextView.text = product.quantity.toString()
        holder.categoryTextView.text = product.description

        holder.btnIncrease.setOnClickListener {
            product.quantity++
            FirebaseHelper.updateProductQuantityInFirebase(product)
            notifyItemChanged(position)
        }

        holder.btnDecrease.setOnClickListener {
            if (product.quantity > 0) {
                product.quantity--
                FirebaseHelper.updateProductQuantityInFirebase(product)
                notifyItemChanged(position)
                if (product.quantity == 0) {
                    onZeroQuantity(product)
                }
            }
        }

        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount() = productListFiltrada.size

    fun removeProduct(product: Product) {
        val position = productListFiltrada.indexOf(product)
        if (position >= 0) {
            productListFiltrada.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateProducts(newProducts: List<Product>) {
        productList.clear()
        productList.addAll(newProducts)
        productListFiltrada = productList.toMutableList()
        notifyDataSetChanged()
    }

    fun addProduto(produto: String) {
        val novoProduto = Product(name = produto, quantity = 0)
        productList.add(0, novoProduto)
        productListFiltrada.add(0, novoProduto)
        notifyItemInserted(0)
    }

    fun filtrar(query: String) {
        productListFiltrada = if (query.isEmpty()) {
            productList.toMutableList()
        } else {
            productList.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
