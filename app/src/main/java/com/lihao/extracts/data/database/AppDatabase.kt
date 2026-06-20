package com.lihao.extracts.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lihao.extracts.data.dao.CategoryDao
import com.lihao.extracts.data.dao.NoteDao
import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.data.entity.Note

@Database(
    entities = [Category::class, Note::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建新表，包含外键约束
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notes_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `content` TEXT NOT NULL,
                        `source` TEXT NOT NULL DEFAULT '',
                        `categoryId` INTEGER NOT NULL,
                        `updateTime` INTEGER NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // 创建索引
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_categoryId` ON `notes_new` (`categoryId`)")
                
                // 复制数据
                database.execSQL("""
                    INSERT INTO `notes_new` (`id`, `content`, `source`, `categoryId`, `updateTime`)
                    SELECT `id`, `content`, `source`, `categoryId`, `updateTime` FROM `notes`
                """.trimIndent())
                
                // 删除旧表
                database.execSQL("DROP TABLE `notes`")
                
                // 重命名新表
                database.execSQL("ALTER TABLE `notes_new` RENAME TO `notes`")
                
                // 重命名后重新创建索引（因为 RENAME 后索引会丢失）
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_categoryId` ON `notes` (`categoryId`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "extracts_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
