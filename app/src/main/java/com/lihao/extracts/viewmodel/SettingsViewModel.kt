package com.lihao.extracts.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lihao.extracts.ExtractsApplication
import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.data.entity.Note
import com.lihao.extracts.utils.BackupHelper
import com.lihao.extracts.utils.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ExtractsApplication).repository
    private val settingsManager = SettingsManager(application)

    val widgetRefreshHour: StateFlow<Int> = settingsManager.widgetRefreshHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val widgetOpacity: StateFlow<Int> = settingsManager.widgetOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setWidgetRefreshHour(hour: Int) {
        viewModelScope.launch {
            settingsManager.setWidgetRefreshHour(hour)
        }
    }

    fun setWidgetOpacity(opacity: Int) {
        viewModelScope.launch {
            settingsManager.setWidgetOpacity(opacity)
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val categories = repository.getAllCategories().first()
                val notes = repository.getAllNotes().first()
                val json = BackupHelper.exportToJson(categories, notes)
                getApplication<ExtractsApplication>().contentResolver
                    .openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                _message.value = "导出成功"
            } catch (e: Exception) {
                _message.value = "导出失败: ${e.message}"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<ExtractsApplication>()
                
                // 读取 JSON 数据
                val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                    withContext(Dispatchers.Main) {
                        _message.value = "导入失败: 无法读取文件"
                    }
                    return@launch
                }
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                reader.close()
                
                val backup = BackupHelper.importFromJson(json)

                // 数据库操作
                val db = context.database
                
                // 清空所有表
                db.clearAllTables()

                // 重新插入默认分类
                repository.insertCategory(Category(name = "未分类", isSystem = true))

                // 插入分类（跳过系统分类，已创建）
                val categoryMap = mutableMapOf<Long, Long>() // old id -> new id
                val defaultCategory = repository.getDefaultCategory()
                defaultCategory?.let { categoryMap[it.id] = it.id }

                for (cat in backup.categories) {
                    if (!cat.isSystem) {
                        val newId = repository.insertCategory(
                            Category(name = cat.name, isSystem = false)
                        )
                        categoryMap[cat.id] = newId
                    }
                }

                // 插入摘记，映射分类 ID
                for (note in backup.notes) {
                    val newCategoryId = categoryMap[note.categoryId]
                        ?: defaultCategory?.id ?: continue
                    repository.insertNote(
                        Note(
                            content = note.content,
                            source = note.source,
                            categoryId = newCategoryId,
                            updateTime = note.updateTime
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    _message.value = "导入成功"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = "${e.javaClass.simpleName}: ${e.message ?: "未知错误"}"
                    _message.value = "导入失败: $errorMsg"
                }
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
