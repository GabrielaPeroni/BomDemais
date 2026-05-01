package com.estoque.bomdemais.listadecompras

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.ShoppingItem
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class ListaDeComprasAdapter(
    private val items: MutableList<ShoppingItem>,
    private val onCheckedChanged: (ShoppingItem) -> Unit,
    private val onQuantityChanged: (ShoppingItem) -> Unit,
    private val onLongPress: (ShoppingItem) -> Unit,
    private val onSelectionChanged: (count: Int) -> Unit
) : RecyclerView.Adapter<ListaDeComprasAdapter.ItemViewHolder>() {

    var isSelectionMode = false
        private set
    private val selectedIds = mutableSetOf<String>()

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.text_item_name)
        val textCategory: TextView = view.findViewById(R.id.text_item_category)
        val textQty: TextView = view.findViewById(R.id.text_qty_to_buy)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
        val stepperLayout: View = view.findViewById(R.id.stepper_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        val isSelected = selectedIds.contains(item.id)

        holder.textName.text = item.name
        holder.textCategory.text = item.category.ifEmpty { "Manual" }
        holder.textQty.text = item.quantityToBuy.toString()
        holder.stepperLayout.visibility = if (isSelectionMode) View.GONE else View.VISIBLE

        if (item.isChecked) {
            holder.textName.paintFlags = holder.textName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.5f
        } else {
            holder.textName.paintFlags = holder.textName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.alpha = 1f
        }

        (holder.itemView as MaterialCardView).setCardBackgroundColor(
            if (isSelected) ContextCompat.getColor(holder.itemView.context, R.color.card_selected_bg)
            else MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSurface)
        )

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            if (isSelectionMode) {
                toggleSelection(item.id)
            } else {
                item.isChecked = !item.isChecked
                onCheckedChanged(item)
                sortCheckedToBottom()
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) onLongPress(item)
            true
        }

        holder.btnIncrease.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            item.quantityToBuy++
            onQuantityChanged(item)
            notifyItemChanged(pos)
        }

        holder.btnDecrease.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            if (item.quantityToBuy > 1) {
                item.quantityToBuy--
                onQuantityChanged(item)
                notifyItemChanged(pos)
            }
        }
    }

    override fun getItemCount() = items.size

    fun getItemAt(position: Int): ShoppingItem = items[position]

    fun updateItems(newItems: List<ShoppingItem>) {
        items.clear()
        items.addAll(newItems.sortedBy { it.isChecked })
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

    fun getSelectedItems(): List<ShoppingItem> = items.filter { selectedIds.contains(it.id) }

    private fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }

    private fun sortCheckedToBottom() {
        items.sortBy { it.isChecked }
        notifyDataSetChanged()
    }
}
