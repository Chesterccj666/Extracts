package com.lihao.extracts.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lihao.extracts.data.dao.CategoryDao
import com.lihao.extracts.data.dao.NoteDao
import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.data.entity.Note

@Database(
    entities = [Category::class, Note::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "extracts_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
