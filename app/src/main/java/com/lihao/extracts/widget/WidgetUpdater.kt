package com.lihao.extracts.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.lihao.extracts.data.database.AppDatabase
import com.lihao.extracts.utils.SettingsManager
import kotlinx.coroutines.flow.first

suspend fun updateWidget(context: Context) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val widgetIds = appWidgetManager.getAppWidgetIds(
        ComponentName(context, ExtractsWidgetReceiver::class.java)
    )

    if (widgetIds.isEmpty()) return

    val db = AppDatabase.getInstance(context)
    val randomNote = db.noteDao().getRandomNote()
    val settingsManager = SettingsManager(context)
    val opacity = settingsManager.widgetOpacity.first()
    val contentColor = settingsManager.widgetContentColor.first()
    val sourceColor = settingsManager.widgetSourceColor.first()

    val views = buildWidgetRemoteViews(
        context = context,
        noteContent = randomNote?.content ?: "暂无摘记",
        noteSource = randomNote?.source ?: "",
        opacity = opacity,
        contentColor = contentColor,
        sourceColor = sourceColor
    )

    appWidgetManager.updateAppWidget(widgetIds, views)
}
