package com.estoque.bomdemais.financeiro

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FinanceiroActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FinanceiroAdapter
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var totalTextView: TextView // Total amount TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financeiro)

        // Initialize the return button
        val returnButton = findViewById<ImageView>(R.id.btn_return)
        returnButton.setOnClickListener {
            finish() // Return to MainActivity
        }

        // Initialize the total TextView
        totalTextView = findViewById(R.id.text_total_sum)

        // Configuring RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FinanceiroAdapter(mutableListOf()) // Initialize with an empty list
        recyclerView.adapter = adapter

        // Configuring FloatingActionButton
        fabAddTransaction = findViewById(R.id.fab_add_financeiro)
        fabAddTransaction.setOnClickListener {
            showAddFinanceiroDialog()
        }
    }

    private fun showAddFinanceiroDialog() {
        val dialog = AddFinanceiroDialog()
        dialog.setTargetFragment(null, 0)
        dialog.show(supportFragmentManager, "AddFinanceiroDialog")
    }

    // This method will be called from the dialog
    fun addTransaction(amount: Double) {
        adapter.adicionarTransacao(amount)
        updateTotal()
        recyclerView.scrollToPosition(0)
    }
    

    private fun updateTotal() {
        val total = adapter.calcularTotal()
        totalTextView.text = "Total: R$${"%.2f".format(total)}" // Update the total amount
    }
}