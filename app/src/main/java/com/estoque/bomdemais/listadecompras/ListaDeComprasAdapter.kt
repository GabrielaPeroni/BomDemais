package com.estoque.bomdemais.listadecompras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.ShoppingItem

class ListaDeComprasAdapter(
    private val items: MutableList<ShoppingItem>,
    private val onDelete: (ShoppingItem) -> Unit,
    private val onQuantityChanged: (ShoppingItem) -> Unit
) : RecyclerView.Adapter<ListaDeComprasAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.text_item_name)
        val textCategory: TextView = view.findViewById(R.id.text_item_category)
        val textQty: TextView = view.findViewById(R.id.text_qty_to_buy)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        holder.textName.text = item.name
        holder.textCategory.text = item.category.ifEmpty { "Manual" }
        holder.textQty.text = item.quantityToBuy.toString()

        holder.btnIncrease.setOnClickListener {
            item.quantityToBuy++
            onQuantityChanged(item)
            notifyItemChanged(position)
        }

        holder.btnDecrease.setOnClickListener {
            if (item.quantityToBuy > 1) {
                item.quantityToBuy--
                onQuantityChanged(item)
                notifyItemChanged(position)
            }
        }

        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ShoppingItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: ShoppingItem) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun removeItem(item: ShoppingItem) {
        val position = items.indexOf(item)
        if (position >= 0) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
