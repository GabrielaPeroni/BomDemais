package com.estoque.bomdemais.listadecompras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.ShoppingItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaDeComprasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListaDeComprasAdapter
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_lista_de_compras, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Lista de Compras"

        firebaseHelper = FirebaseHelper()

        recyclerView = view.findViewById(R.id.recycler_view_lista)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ListaDeComprasAdapter(
            mutableListOf(),
            onDelete = { item ->
                firebaseHelper.deleteShoppingItem(item.id)
                adapter.removeItem(item)
            },
            onQuantityChanged = { item ->
                firebaseHelper.updateShoppingItemQuantity(item)
            }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab_add_item).setOnClickListener {
            showAddItemDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadListFromFirebase()
    }

    private fun showAddItemDialog() {
        val input = EditText(requireContext())
        input.hint = "Nome do item"

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Adicionar à lista")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val item = ShoppingItem(name = name, category = "", quantityToBuy = 1)
                    firebaseHelper.addShoppingItem(item) { success ->
                        if (success) loadListFromFirebase()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadListFromFirebase() {
        firebaseHelper.getShoppingItems { items ->
            adapter.updateItems(items)
        }
    }
}
