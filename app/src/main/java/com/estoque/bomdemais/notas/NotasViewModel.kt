package com.estoque.bomdemais.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.estoque.bomdemais.data.Note
import com.estoque.bomdemais.data.NotasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotasViewModel(private val repo: NotasRepository) : ViewModel() {

    val notes: StateFlow<List<Note>> = repo.notes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addNote(text: String) = viewModelScope.launch { repo.addNote(text) }
    fun deleteNote(note: Note) = viewModelScope.launch { repo.deleteNote(note.id) }
    fun restoreNote(note: Note) = viewModelScope.launch { repo.restoreNote(note) }
    fun editNote(note: Note, newText: String) = viewModelScope.launch { repo.editNote(note, newText) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { NotasViewModel(NotasRepository()) }
        }
    }
}
