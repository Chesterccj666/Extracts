package com.lihao.extracts.widget

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 处理小组件刷新按钮点击的广播接收器
 * 不启动任何 Activity，直接在后台更新小组件
 */
class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            CoroutineScope(Dispatchers.IO).launch {
                updateWidget(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.lihao.extracts.widget.ACTION_REFRESH"

        /**
         * 创建刷新操作的 PendingIntent
         */
        fun getRefreshPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, WidgetRefreshReceiver::class.java).apply {
                action = ACTION_REFRESH
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
