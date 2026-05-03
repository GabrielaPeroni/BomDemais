package com.estoque.bomdemais.financeiro

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Transaction
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onTap: (Transaction) -> Unit,
    private val onLongPress: (Transaction) -> Unit,
    private val onSelectionChanged: (count: Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    var isSelectionMode = false
        private set
    private val selectedIds = mutableSetOf<String>()

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeIndicator: TextView = view.findViewById(R.id.text_type_indicator)
        val textDescription: TextView = view.findViewById(R.id.text_description)
        val textDate: TextView = view.findViewById(R.id.text_date)
        val textAmount: TextView = view.findViewById(R.id.text_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transacao, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val t = transactions[position]
        val isSelected = selectedIds.contains(t.id)
        val ctx = holder.itemView.context

        if (t.type == "RECEITA") {
            holder.typeIndicator.text = "↑"
            holder.typeIndicator.setTextColor(Color.parseColor("#FFBD5F"))
            holder.textAmount.setTextColor(Color.parseColor("#FFBD5F"))
        } else {
            holder.typeIndicator.text = "↓"
            holder.typeIndicator.setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_light_tertiary))
            holder.textAmount.setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_light_tertiary))
        }

        holder.textDescription.text = t.description.ifEmpty { t.type }
        holder.textDate.text = dateFmt.format(Date(t.date))
        holder.textAmount.text = String.format(Locale("pt", "BR"), "R$ %.2f", t.amount)

        (holder.itemView as MaterialCardView).setCardBackgroundColor(
            if (isSelected) ContextCompat.getColor(ctx, R.color.card_selected_bg)
            else MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSurface)
        )

        holder.itemView.setOnClickListener {
            if (isSelectionMode) toggleSelection(t.id)
            else onTap(t)
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) onLongPress(t)
            true
        }
    }

    override fun getItemCount() = transactions.size

    fun getItemAt(position: Int): Transaction = transactions[position]

    fun updateTransactions(newList: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newList)
        notifyDataSetChanged()
    }

    fun addTransaction(t: Transaction) {
        transactions.add(0, t)
        notifyItemInserted(0)
    }

    fun removeTransaction(t: Transaction) {
        val pos = transactions.indexOf(t)
        if (pos >= 0) {
            transactions.removeAt(pos)
            notifyItemRemoved(pos)
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

    fun getSelectedItems(): List<Transaction> = transactions.filter { selectedIds.contains(it.id) }

    private fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }
}
