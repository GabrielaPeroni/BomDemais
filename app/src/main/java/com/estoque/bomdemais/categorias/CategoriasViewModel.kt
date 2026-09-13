package com.estoque.bomdemais.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.estoque.bomdemais.data.Category
import com.estoque.bomdemais.data.CategoriasRepository
import com.estoque.bomdemais.data.Product
import com.estoque.bomdemais.data.ProdutosRepository
import com.estoque.bomdemais.data.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriasViewModel(
    private val repo: CategoriasRepository,
    private val produtosRepo: ProdutosRepository,
    private val shoppingRepo: ShoppingRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repo.categories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCategory(name: String) = viewModelScope.launch { repo.addCategory(name) }

    suspend fun getProductsForCategories(categories: List<Category>): List<Product> =
        categories.flatMap { produtosRepo.getProductsByCategory(it.name) }

    fun deleteCategoriesWithCascade(categories: List<Category>, products: List<Product>) =
        viewModelScope.launch {
            val productNames = products.map { it.name }.toSet()
            if (productNames.isNotEmpty()) shoppingRepo.deleteItemsByNames(productNames)
            products.forEach { produtosRepo.deleteProduct(it.id) }
            categories.forEach { repo.deleteCategory(it) }
        }

    fun renameCategory(category: Category, newName: String, onResult: (Boolean) -> Unit) =
        viewModelScope.launch { onResult(repo.renameCategory(category, newName)) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CategoriasViewModel(CategoriasRepository(), ProdutosRepository(), ShoppingRepository())
            }
        }
    }
}
