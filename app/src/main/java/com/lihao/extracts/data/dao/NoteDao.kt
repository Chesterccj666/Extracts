package com.lihao.extracts.data.dao

import androidx.room.*
import com.lihao.extracts.data.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updateTime DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE categoryId = :categoryId ORDER BY updateTime DESC")
    fun getNotesByCategory(categoryId: Long): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE content LIKE '%' || :query || '%' 
        OR source LIKE '%' || :query || '%' 
        OR author LIKE '%' || :query || '%' 
        ORDER BY updateTime DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE categoryId = :categoryId 
        AND (content LIKE '%' || :query || '%' 
        OR source LIKE '%' || :query || '%' 
        OR author LIKE '%' || :query || '%')
        ORDER BY updateTime DESC
    """)
    fun searchNotesByCategory(query: String, categoryId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomNote(): Note?

    @Query("UPDATE notes SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun moveNotesToCategory(oldCategoryId: Long, newCategoryId: Long)
}
