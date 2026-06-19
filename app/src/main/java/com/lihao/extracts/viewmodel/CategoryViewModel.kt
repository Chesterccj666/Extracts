package com.lihao.extracts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lihao.extracts.ExtractsApplication
import com.lihao.extracts.data.entity.Category
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ExtractsApplication).repository

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCategory(Category(name = name.trim()))
        }
    }

    fun updateCategory(category: Category, newName: String) {
        if (newName.isBlank() || category.isSystem) return
        viewModelScope.launch {
            repository.updateCategory(category.copy(name = newName.trim()))
        }
    }

    fun deleteCategory(category: Category) {
        if (category.isSystem) return
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
