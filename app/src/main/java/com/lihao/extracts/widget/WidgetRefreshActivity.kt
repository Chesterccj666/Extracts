package com.lihao.extracts.widget

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 透明 Activity，用于处理小组件刷新按钮点击
 * 配置为 noHistory + excludeFromRecents，不会出现在最近任务中
 * 在 onCreate 中立即 finish()，用户看不到任何界面
 */
class WidgetRefreshActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 立即完成，不显示任何 UI
        finish()

        // 在后台执行刷新逻辑
        CoroutineScope(Dispatchers.IO).launch {
            updateWidget(this@WidgetRefreshActivity)
        }
    }
}
