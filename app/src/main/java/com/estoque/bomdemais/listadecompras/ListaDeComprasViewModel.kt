package com.estoque.bomdemais.listadecompras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.estoque.bomdemais.data.ShoppingItem
import com.estoque.bomdemais.data.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListaDeComprasViewModel(private val repo: ShoppingRepository) : ViewModel() {

    val items: StateFlow<List<ShoppingItem>> = repo.shoppingItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addItem(name: String) = viewModelScope.launch { repo.addItem(name) }
    fun addItemFromProduct(name: String, category: String) = viewModelScope.launch { repo.addItem(name, category) }
    fun deleteItem(item: ShoppingItem) = viewModelScope.launch { repo.deleteItem(item.id) }
    fun restoreItem(item: ShoppingItem) = viewModelScope.launch { repo.restoreItem(item) }
    fun toggleChecked(item: ShoppingItem) = viewModelScope.launch {
        item.isChecked = !item.isChecked
        repo.updateChecked(item)
    }
    fun updateQuantity(item: ShoppingItem) = viewModelScope.launch { repo.updateQuantity(item) }
    fun renameItem(item: ShoppingItem, newName: String) = viewModelScope.launch { repo.renameItem(item, newName) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ListaDeComprasViewModel(ShoppingRepository()) }
        }
    }
}
