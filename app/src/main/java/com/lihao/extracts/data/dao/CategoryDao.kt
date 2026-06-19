package com.lihao.extracts.data.dao

import androidx.room.*
import com.lihao.extracts.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isSystem DESC, name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE isSystem = 1 LIMIT 1")
    suspend fun getDefaultCategory(): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT COUNT(*) FROM notes WHERE categoryId = :categoryId")
    suspend fun getNoteCountByCategory(categoryId: Long): Int
}
