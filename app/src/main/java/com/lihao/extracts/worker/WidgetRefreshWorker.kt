package com.lihao.extracts.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lihao.extracts.widget.updateWidget

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            updateWidget(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
