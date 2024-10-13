package com.estoque.bomdemais.estoque

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.R

class AddEstoqueDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_estoque, null)

        val editTextName = view.findViewById<EditText>(R.id.edit_text_product_name)
        val editTextCategory = view.findViewById<EditText>(R.id.edit_text_product_category)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Adicionar Produto")
            .setNegativeButton("Cancelar", null)
            .create().apply {
                setOnShowListener {
                    btnConfirm.setOnClickListener {
                        val name = editTextName.text.toString()
                        val category = editTextCategory.text.toString()

                        if (name.isNotEmpty() && category.isNotEmpty()) {
                            FirebaseHelper().addProduct(name, category)
                            dismiss() // Close the dialog after adding the product
                        }
                    }
                }
            }
    }
}
