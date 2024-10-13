package com.estoque.bomdemais.financeiro

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.estoque.bomdemais.R

class AddFinanceiroDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_add_financeiro, null)

        val editTextAmount = view.findViewById<EditText>(R.id.input_amount)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel) // Make sure this button exists in your layout

        btnConfirm.setOnClickListener {
            val amountString = editTextAmount.text.toString()
            if (amountString.isNotEmpty()) {
                val amount = amountString.toDouble()
                (activity as FinanceiroActivity).addTransaction(amount)
                dismiss()
            }
        }
        btnCancel.setOnClickListener {
            dismiss() // Dismiss the dialog on cancel
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Adicionar Transação")
            .create()
    }
}
