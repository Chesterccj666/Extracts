package com.lihao.extracts.worker

import android.content.Context
import androidx.work.*
import com.lihao.extracts.utils.SettingsManager
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WidgetScheduler {
    private const val WORK_NAME = "widget_refresh_work"

    suspend fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val settingsManager = SettingsManager(context)
        val refreshHour = settingsManager.widgetRefreshHour.first()

        // Calculate next refresh time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, refreshHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
