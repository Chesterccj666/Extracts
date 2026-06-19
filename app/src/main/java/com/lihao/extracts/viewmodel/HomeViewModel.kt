package com.lihao.extracts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lihao.extracts.ExtractsApplication
import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.data.entity.Note
import com.lihao.extracts.widget.updateWidget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ExtractsApplication).repository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = combine(
        _searchQuery,
        _selectedCategory,
        repository.getAllCategories()
    ) { query, categoryId, _ ->
        Pair(query, categoryId)
    }.flatMapLatest { (query, categoryId) ->
        when {
            query.isNotBlank() && categoryId != null ->
                repository.searchNotesByCategory(query, categoryId)
            query.isNotBlank() -> repository.searchNotes(query)
            categoryId != null -> repository.getNotesByCategory(categoryId)
            else -> repository.getAllNotes()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun refreshWidget() {
        viewModelScope.launch {
            updateWidget(getApplication())
        }
    }
}
