package com.estoque.bomdemais.financeiro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R

class FinanceiroAdapter(private var transacoes: MutableList<Double>) : RecyclerView.Adapter<FinanceiroAdapter.TransacaoViewHolder>() {

    class TransacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTransacao: TextView = view.findViewById(R.id.text_transacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transacao, parent, false)
        return TransacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransacaoViewHolder, position: Int) {
        val transacao = transacoes[position]
        holder.textViewTransacao.text = "+ R$${"%.2f".format(transacao)}"
    }

    override fun getItemCount() = transacoes.size

    fun adicionarTransacao(valor: Double) {
        transacoes.add(0, valor) // Add at the top of the list
        notifyItemInserted(0)
    }

    fun calcularTotal(): Double {
        return transacoes.sum()
    }
}
