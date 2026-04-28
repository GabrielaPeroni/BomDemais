package com.estoque.bomdemais.listadecompras

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.ShoppingItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaDeComprasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListaDeComprasAdapter
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_de_compras)

        firebaseHelper = FirebaseHelper()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recycler_view_lista)
        recyclerView.layoutManager = LinearLayoutManager(this)

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

        findViewById<FloatingActionButton>(R.id.fab_add_item).setOnClickListener {
            showAddItemDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadListFromFirebase()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showAddItemDialog() {
        val input = EditText(this)
        input.hint = "Nome do item"

        MaterialAlertDialogBuilder(this)
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
