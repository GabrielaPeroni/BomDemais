package com.estoque.bomdemais.financeiro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionBottomSheet : BottomSheetDialogFragment() {

    var onSave: ((type: String, amount: Double, description: String, date: Long) -> Unit)? = null
    var onDelete: (() -> Unit)? = null

    private var selectedDate: Long = MaterialDatePicker.todayInUtcMilliseconds()
    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    companion object {
        fun newInstance(transaction: Transaction? = null) = AddTransactionBottomSheet().apply {
            transaction?.let {
                arguments = Bundle().apply {
                    putString("type", it.type)
                    putDouble("amount", it.amount)
                    putString("description", it.description)
                    putLong("date", it.date)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_add_transaction, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textTitle = view.findViewById<TextView>(R.id.text_sheet_title)
        val btnDelete = view.findViewById<ImageButton>(R.id.btn_delete_transaction)
        val toggleType = view.findViewById<MaterialButtonToggleGroup>(R.id.toggle_type)
        val editAmount = view.findViewById<TextInputEditText>(R.id.edit_amount)
        val editDescription = view.findViewById<TextInputEditText>(R.id.edit_description)
        val editDate = view.findViewById<TextInputEditText>(R.id.edit_date)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save)

        val args = arguments

        if (args != null) {
            textTitle.text = "Editar Lançamento"
            btnSave.text = "Salvar alterações"
            btnDelete.visibility = View.VISIBLE

            val type = args.getString("type", "RECEITA")
            val amount = args.getDouble("amount", 0.0)
            val description = args.getString("description", "")
            val date = args.getLong("date", selectedDate)

            toggleType.check(if (type == "RECEITA") R.id.btn_receita else R.id.btn_despesa)
            editAmount.setText(String.format(Locale.getDefault(), "%.2f", amount))
            editDescription.setText(description)
            selectedDate = date
        } else {
            toggleType.check(R.id.btn_receita)
        }

        editDate.setText(dateFmt.format(Date(selectedDate)))

        editDate.setOnClickListener { showDatePicker(editDate) }

        btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir lançamento?")
                .setMessage("Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir") { _, _ ->
                    onDelete?.invoke()
                    dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnSave.setOnClickListener {
            val type = if (toggleType.checkedButtonId == R.id.btn_receita) "RECEITA" else "DESPESA"
            val amountStr = editAmount.text?.toString()?.replace(",", ".") ?: ""
            val amount = amountStr.toDoubleOrNull()
            val description = editDescription.text?.toString()?.trim() ?: ""

            if (amount == null || amount <= 0) {
                editAmount.error = "Informe um valor válido"
                return@setOnClickListener
            }

            onSave?.invoke(type, amount, description, selectedDate)
            dismiss()
        }
    }

    private fun showDatePicker(editDate: TextInputEditText) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecionar data")
            .setSelection(selectedDate)
            .build()
        picker.addOnPositiveButtonClickListener { utcMs ->
            selectedDate = utcMs
            editDate.setText(dateFmt.format(Date(utcMs)))
        }
        picker.show(parentFragmentManager, "date_picker")
    }
}
