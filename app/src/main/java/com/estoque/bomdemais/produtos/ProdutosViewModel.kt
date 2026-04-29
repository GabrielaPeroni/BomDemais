package com.estoque.bomdemais.produtos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.estoque.bomdemais.data.Product
import com.estoque.bomdemais.data.ProdutosRepository
import com.estoque.bomdemais.data.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProdutosViewModel(
    private val repo: ProdutosRepository,
    private val shoppingRepo: ShoppingRepository,
    category: String
) : ViewModel() {

    val products: StateFlow<List<Product>> = repo.productsByCategory(category)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addProduct(name: String, category: String) = viewModelScope.launch { repo.addProduct(name, category) }
    fun deleteProduct(product: Product) = viewModelScope.launch { repo.deleteProduct(product.id) }
    fun restoreProduct(product: Product) = viewModelScope.launch { repo.restoreProduct(product) }
    fun updateQuantity(product: Product) = viewModelScope.launch { repo.updateQuantity(product) }
    fun renameProduct(product: Product, newName: String) = viewModelScope.launch { repo.renameProduct(product, newName) }
    fun addToShoppingList(name: String, category: String) = viewModelScope.launch { shoppingRepo.addItem(name, category) }

    companion object {
        fun factory(category: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProdutosViewModel(ProdutosRepository(), ShoppingRepository(), category) }
        }
    }
}
