package com.lihao.extracts

import android.app.Application
import com.lihao.extracts.data.database.AppDatabase
import com.lihao.extracts.data.entity.Category
import com.lihao.extracts.repository.AppRepository
import com.lihao.extracts.worker.WidgetScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExtractsApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: AppRepository by lazy {
        AppRepository(database.categoryDao(), database.noteDao())
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            initDefaultCategory()
            WidgetScheduler.schedule(this@ExtractsApplication)
        }
    }

    private suspend fun initDefaultCategory() {
        val dao = database.categoryDao()
        val default = dao.getDefaultCategory()
        if (default == null) {
            dao.insert(Category(name = "未分类", isSystem = true))
        }
    }
}
