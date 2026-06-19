package com.lihao.extracts.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

suspend fun updateWidget(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(ExtractsWidget::class.java)
    
    // 为每个 widget 加载新的随机摘记并立即更新
    glanceIds.forEach { id ->
        ExtractsWidget.refreshWidget(context, id)
    }
}
