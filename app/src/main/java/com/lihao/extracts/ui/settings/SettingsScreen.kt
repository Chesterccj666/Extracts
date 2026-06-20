package com.lihao.extracts.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lihao.extracts.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

/**
 * 自定义透明度滑块组件
 * 样式：一条细线 + 可拖动的小圆点，使用暖色文学主题配色
 */
@Composable
fun OpacitySlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..100,
    thumbRadiusDp: Int = 10,
    trackHeightDp: Int = 2
) {
    val fraction = (value - range.first).toFloat() / (range.last - range.first)
    val trackColor = Color(0xFFC9BFA8) // 暖灰色轨道
    val thumbColor = Color(0xFF8B5A3E) // 暖棕色圆点
    val trackThumbRadiusDp = thumbRadiusDp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineHeight = trackHeightDp.dp.toPx()
            val thumbRadiusPx = trackThumbRadiusDp.dp.toPx()
            val startY = size.height / 2
            val startX = thumbRadiusPx
            val endX = size.width - thumbRadiusPx
            val thumbX = startX + (endX - startX) * fraction

            // 绘制轨道线
            drawLine(
                color = trackColor,
                start = Offset(startX, startY),
                end = Offset(endX, startY),
                strokeWidth = lineHeight,
                cap = StrokeCap.Round
            )

            // 绘制圆点
            drawCircle(
                color = thumbColor,
                radius = thumbRadiusPx,
                center = Offset(thumbX, startY)
            )
        }

        // 拖拽交互 - 使用绝对位置计算，确保实时跟随
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val thumbRadiusPx = trackThumbRadiusDp.dp.toPx()
                        val startX = thumbRadiusPx
                        val endX = size.width - thumbRadiusPx
                        // 使用绝对触摸位置计算，而非增量
                        val touchX = change.position.x.coerceIn(startX, endX)
                        val newFraction = (touchX - startX) / (endX - startX)
                        val newValue = (range.first + (range.last - range.first) * newFraction)
                            .roundToInt()
                            .coerceIn(range)
                        if (newValue != value) {
                            onValueChange(newValue)
                        }
                        change.consume()
                    }
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val refreshHour by viewModel.widgetRefreshHour.collectAsState()
    val widgetOpacity by viewModel.widgetOpacity.collectAsState()
    val message by viewModel.message.collectAsState()

    var showHourPicker by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var localOpacity by remember { mutableIntStateOf(widgetOpacity) }

    // 同步本地透明度值
    LaunchedEffect(widgetOpacity) {
        localOpacity = widgetOpacity
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportConfirm = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Widget refresh time setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Widget 刷新时间",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "每天 ${String.format("%02d:00", refreshHour)} 自动刷新",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showHourPicker = true }) {
                        Text("修改", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Widget opacity setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FormatColorFill,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "小组件背景透明度",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "当前透明度: $localOpacity%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OpacitySlider(
                        value = localOpacity,
                        onValueChange = { localOpacity = it },
                        modifier = Modifier.fillMaxWidth(),
                        range = 0..100
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            viewModel.setWidgetOpacity(localOpacity)
                        }) {
                            Text("应用", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Export backup
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "导出备份",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "将所有数据导出为 JSON 文件",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { exportLauncher.launch("extracts_backup.json") }) {
                        Text("导出", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            // Import backup
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "导入备份",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "从 JSON 文件导入数据（将覆盖当前数据）",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                        Text("导入", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }

    // Hour picker dialog
    if (showHourPicker) {
        var selectedHour by remember { mutableIntStateOf(refreshHour) }

        AlertDialog(
            onDismissRequest = { showHourPicker = false },
            title = {
                Text(
                    "选择刷新时间",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "当前: ${String.format("%02d:00", selectedHour)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = selectedHour.toFloat(),
                        onValueChange = { selectedHour = it.toInt() },
                        valueRange = 0f..23f,
                        steps = 23,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setWidgetRefreshHour(selectedHour)
                    showHourPicker = false
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHourPicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Import confirmation dialog
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirm = false
                pendingImportUri = null
            },
            title = {
                Text(
                    "确认导入",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { Text("导入将覆盖当前所有数据，是否继续？") },
            confirmButton = {
                TextButton(onClick = {
                    pendingImportUri?.let { viewModel.importBackup(it) }
                    showImportConfirm = false
                    pendingImportUri = null
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    pendingImportUri = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    // Message snackbar
    message?.let {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("关闭")
                }
            }
        ) {
            Text(it)
        }
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
}
