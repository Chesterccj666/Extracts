package com.lihao.extracts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lihao.extracts.ExtractsApplication
import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.data.entity.Note
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ExtractsApplication).repository

    private val _noteId = MutableStateFlow<Long?>(null)

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _source = MutableStateFlow("")
    val source: StateFlow<String> = _source.asStateFlow()

    private val _author = MutableStateFlow("")
    val author: StateFlow<String> = _author.asStateFlow()

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun loadNote(noteId: Long) {
        _noteId.value = noteId
        viewModelScope.launch {
            val note = repository.getNoteById(noteId) ?: return@launch
            _content.value = note.content
            _source.value = note.source
            _author.value = note.author
            _categoryId.value = note.categoryId
        }
    }

    fun setContent(value: String) { _content.value = value }
    fun setSource(value: String) { _source.value = value }
    fun setAuthor(value: String) { _author.value = value }
    fun setCategoryId(value: Long) { _categoryId.value = value }

    fun save() {
        viewModelScope.launch {
            val catId = _categoryId.value ?: return@launch
            if (_content.value.isBlank() || _source.value.isBlank()) return@launch

            val noteId = _noteId.value
            if (noteId != null) {
                repository.updateNote(
                    Note(
                        id = noteId,
                        content = _content.value,
                        source = _source.value,
                        author = _author.value,
                        categoryId = catId,
                        updateTime = System.currentTimeMillis()
                    )
                )
            } else {
                repository.insertNote(
                    Note(
                        content = _content.value,
                        source = _source.value,
                        author = _author.value,
                        categoryId = catId
                    )
                )
            }
            _saved.value = true
        }
    }

    fun reset() {
        _noteId.value = null
        _content.value = ""
        _source.value = ""
        _author.value = ""
        _categoryId.value = null
        _saved.value = false
    }
}
