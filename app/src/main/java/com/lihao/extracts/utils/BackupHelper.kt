package com.lihao.extracts.utils

import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.data.entity.Note
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CategoryJson(
    val id: Long = 0,
    val name: String,
    val isSystem: Boolean = false
)

@Serializable
data class NoteJson(
    val id: Long = 0,
    val content: String,
    val source: String,
    val categoryId: Long,
    val updateTime: Long
)

@Serializable
data class BackupData(
    val categories: List<CategoryJson>,
    val notes: List<NoteJson>
)

object BackupHelper {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun exportToJson(categories: List<Category>, notes: List<Note>): String {
        val backup = BackupData(
            categories = categories.map { CategoryJson(it.id, it.name, it.isSystem) },
            notes = notes.map {
                NoteJson(it.id, it.content, it.source, it.categoryId, it.updateTime)
            }
        )
        return json.encodeToString(backup)
    }

    fun importFromJson(jsonStr: String): BackupData {
        return json.decodeFromString(jsonStr)
    }
}
