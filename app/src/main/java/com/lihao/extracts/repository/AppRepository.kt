package com.lihao.extracts.repository

import com.lihao.extracts.data.dao.CategoryDao
import com.lihao.extracts.data.dao.NoteDao
import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.data.entity.Note
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val categoryDao: CategoryDao,
    private val noteDao: NoteDao
) {
    // Category
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)

    suspend fun getDefaultCategory(): Category? = categoryDao.getDefaultCategory()

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) {
        val defaultCategory = getDefaultCategory() ?: return
        noteDao.moveNotesToCategory(category.id, defaultCategory.id)
        categoryDao.delete(category)
    }

    // Note
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getNotesByCategory(categoryId: Long): Flow<List<Note>> =
        noteDao.getNotesByCategory(categoryId)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    fun searchNotesByCategory(query: String, categoryId: Long): Flow<List<Note>> =
        noteDao.searchNotesByCategory(query, categoryId)

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insert(note)

    suspend fun updateNote(note: Note) = noteDao.update(note)

    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    suspend fun getRandomNote(): Note? = noteDao.getRandomNote()
}
