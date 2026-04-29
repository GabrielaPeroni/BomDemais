package com.estoque.bomdemais.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.estoque.bomdemais.data.CategoriasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriasViewModel(private val repo: CategoriasRepository) : ViewModel() {

    val categories: StateFlow<List<String>> = repo.categories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCategory(name: String) = viewModelScope.launch { repo.addCategory(name) }
    fun deleteCategories(names: List<String>) = viewModelScope.launch {
        names.forEach { repo.deleteCategory(it) }
    }
    fun renameCategory(oldName: String, newName: String, onResult: (Boolean) -> Unit) =
        viewModelScope.launch { onResult(repo.renameCategory(oldName, newName)) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoriasViewModel(CategoriasRepository()) }
        }
    }
}
