package com.lihao.extracts.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.lihao.extracts.MainActivity
import com.lihao.extracts.R
import com.lihao.extracts.data.database.AppDatabase

class ExtractsWidget : GlanceAppWidget() {

    override val stateDefinition = WidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // provideGlance 只负责提供初始内容
        // 状态更新由 refreshWidget 负责，不会在 provideGlance 中重复设置
        
        provideContent {
            WidgetContent()
        }
    }

    companion object {
        suspend fun refreshWidget(context: Context, id: GlanceId) {
            val db = AppDatabase.getInstance(context)
            val randomNote = db.noteDao().getRandomNote()
            
            // 更新状态
            updateAppWidgetState(
                context = context,
                definition = WidgetStateDefinition,
                glanceId = id
            ) {
                WidgetState(
                    noteContent = randomNote?.content ?: "暂无摘记",
                    noteAuthor = randomNote?.author ?: "",
                    noteSource = randomNote?.source ?: ""
                )
            }
            
            // 触发 widget 重新渲染
            ExtractsWidget().update(context, id)
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun WidgetContent() {
    val state = currentState<WidgetState>()
    val fzFontFamily = FontFamily("serif")
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
            .background(ColorProvider(Color(0xFFF8F4EE)))
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (state.noteContent.length > 100) {
                state.noteContent.substring(0, 100) + "..."
            } else {
                state.noteContent
            },
            style = TextStyle(
                color = ColorProvider(Color(0xFF2D2926)),
                fontWeight = FontWeight.Medium,
                fontFamily = fzFontFamily
            ),
            maxLines = 4
        )
        if (state.noteAuthor.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "——${state.noteAuthor}",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF6B6560)),
                    fontFamily = fzFontFamily
                )
            )
        }
        if (state.noteSource.isNotEmpty()) {
            Text(
                text = "《${state.noteSource}》",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF6B6560)),
                    fontFamily = fzFontFamily
                )
            )
        }
    }
}
