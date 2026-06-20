package com.lihao.extracts.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import com.lihao.extracts.MainActivity
import com.lihao.extracts.R

/**
 * 解析 ARGB hex 字符串为 int 颜色值，例如 "FF2D2926"
 */
fun parseHexColorInt(hex: String): Int {
    return try {
        java.lang.Long.parseLong(hex, 16).toInt()
    } catch (e: Exception) {
        0xFF2D2926.toInt() // 默认深色
    }
}

/**
 * 将文字渲染为 Bitmap，使用自定义字体
 */
fun renderTextBitmap(
    text: String,
    textSize: Float,
    textColor: Int,
    typeface: Typeface,
    maxWidth: Int,
    maxLines: Int = Int.MAX_VALUE
): Bitmap {
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        this.color = textColor
        this.typeface = typeface
    }

    val spacingMult = 1.0f
    val spacingAdd = 0f

    var layout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, maxWidth)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(spacingAdd, spacingMult)
        .setIncludePad(false)
        .build()

    // 限制行数
    if (layout.lineCount > maxLines) {
        val end = layout.getLineEnd(maxLines - 1)
        val ellipsized = text.substring(0, end.coerceAtMost(text.length)).trimEnd() + "..."
        layout = StaticLayout.Builder
            .obtain(ellipsized, 0, ellipsized.length, paint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(spacingAdd, spacingMult)
            .setIncludePad(false)
            .build()
    }

    val width = layout.width
    val height = layout.height

    val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    layout.draw(canvas)
    return bitmap
}

/**
 * 构建小组件的 RemoteViews
 */
fun buildWidgetRemoteViews(
    context: Context,
    noteContent: String,
    noteSource: String,
    opacity: Int,
    contentColor: String,
    sourceColor: String
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_layout)

    // 设置背景透明度
    val alpha = (opacity / 100f).coerceIn(0f, 1f)
    val baseColor = 0xFFF8F4EE.toInt()
    val r = Color.red(baseColor)
    val g = Color.green(baseColor)
    val b = Color.blue(baseColor)
    val backgroundColor = Color.argb(alpha, r / 255f, g / 255f, b / 255f)
    views.setInt(R.id.widget_root, "setBackgroundColor", backgroundColor)

    // 加载自定义字体
    val customTypeface = ResourcesCompat.getFont(context, R.font.fzqkbyy) ?: Typeface.SERIF

    // 计算可用宽度（小组件宽度减去 padding）
    // 4格宽度约 250dp，减去左右 padding 32dp = 218dp
    val density = context.resources.displayMetrics.density
    val maxWidthPx = (218 * density).toInt()

    // 渲染摘记内容为 Bitmap
    val displayContent = if (noteContent.length > 100) {
        noteContent.substring(0, 100) + "..."
    } else {
        noteContent
    }
    val contentBitmap = renderTextBitmap(
        text = displayContent,
        textSize = 18f * density,
        textColor = parseHexColorInt(contentColor),
        typeface = customTypeface,
        maxWidth = maxWidthPx,
        maxLines = 4
    )
    views.setImageViewBitmap(R.id.note_content_image, contentBitmap)

    // 渲染来源为 Bitmap
    if (noteSource.isNotEmpty()) {
        val sourceBitmap = renderTextBitmap(
            text = "- $noteSource -",
            textSize = 14f * density,
            textColor = parseHexColorInt(sourceColor),
            typeface = customTypeface,
            maxWidth = maxWidthPx,
            maxLines = 1
        )
        views.setImageViewBitmap(R.id.note_source_image, sourceBitmap)
        views.setViewVisibility(R.id.note_source_image, View.VISIBLE)
    } else {
        views.setViewVisibility(R.id.note_source_image, View.GONE)
    }

    // 点击内容区域打开主界面
    val mainIntent = Intent(context, MainActivity::class.java)
    val mainPendingIntent = PendingIntent.getActivity(
        context,
        0,
        mainIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.content_area, mainPendingIntent)

    // 点击刷新按钮发送广播
    views.setOnClickPendingIntent(
        R.id.refresh_button,
        WidgetRefreshReceiver.getRefreshPendingIntent(context)
    )

    return views
}
