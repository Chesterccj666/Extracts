package com.lihao.extracts.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.lihao.extracts.utils.SettingsManager
import kotlinx.coroutines.flow.first

suspend fun updateWidget(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(ExtractsWidget::class.java)
    val settingsManager = SettingsManager(context)
    val opacity = settingsManager.widgetOpacity.first()
    
    // 为每个 widget 加载新的随机摘记并立即更新
    glanceIds.forEach { id ->
        ExtractsWidget.refreshWidget(context, id, opacity)
    }
}
