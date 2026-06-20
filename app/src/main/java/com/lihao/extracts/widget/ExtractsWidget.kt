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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.lihao.extracts.MainActivity
import com.lihao.extracts.data.database.AppDatabase

class ExtractsWidget : GlanceAppWidget() {

    override val stateDefinition = WidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(id)
        }
    }

    companion object {
        suspend fun refreshWidget(context: Context, id: GlanceId, opacity: Int = 100) {
            val db = AppDatabase.getInstance(context)
            val randomNote = db.noteDao().getRandomNote()
            
            updateAppWidgetState(
                context = context,
                definition = WidgetStateDefinition,
                glanceId = id
            ) {
                WidgetState(
                    noteContent = randomNote?.content ?: "暂无摘记",
                    noteSource = randomNote?.source ?: "",
                    opacity = opacity
                )
            }
            
            ExtractsWidget().update(context, id)
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun WidgetContent(glanceId: GlanceId) {
    val state = currentState<WidgetState>()
    
    // 计算带透明度的背景色
    val alpha = (state.opacity / 100f).coerceIn(0f, 1f)
    val baseColor = Color(0xFFF8F4EE)
    val transparentColor = Color(
        red = baseColor.red,
        green = baseColor.green,
        blue = baseColor.blue,
        alpha = alpha
    )
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(transparentColor))
    ) {
        // 内容区域（居中）
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
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
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp
                ),
                maxLines = 4
            )
            if (state.noteSource.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(12.dp))
                Text(
                    text = "- ${state.noteSource} -",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF6B6560)),
                        fontFamily = FontFamily.Serif
                    )
                )
            }
        }

        // 右上角刷新按钮 - 使用 Row + Spacer 实现右上定位
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Box(
                modifier = GlanceModifier
                    .clickable(actionStartActivity<WidgetRefreshActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↻",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8B5A3E)),
                        fontSize = 32.sp
                    )
                )
            }
        }
    }
}
