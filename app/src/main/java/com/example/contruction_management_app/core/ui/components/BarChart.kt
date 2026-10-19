package com.example.contruction_management_app.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens

data class BarItem(
    val label: String,
    val planned: Float,
    val actual: Float,
    val tooltipText: String
)

@Composable
fun BarChart(
    items: List<BarItem>,
    modifier: Modifier = Modifier,
    unit: String = "m³",
    maxValOverride: Float? = null
) {
    if (items.isEmpty()) return

    val maxVal = maxValOverride ?: items.maxOf { maxOf(it.planned, it.actual) }.coerceAtLeast(10f) * 1.2f
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(CmsTokens.Motion.DurationEmphasized))
    }

    val accentColor = CmsTheme.colors.accent
    val mutedColor = CmsTheme.colors.chartMutedBar
    val dividerColor = CmsTheme.colors.divider
    val textSecondary = CmsTheme.colors.textSecondary
    val textPrimary = CmsTheme.colors.textPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(items) {
                    detectTapGestures { offset ->
                        val barSlotWidth = size.width / items.size
                        val index = (offset.x / barSlotWidth).toInt()
                        selectedIndex = if (index in items.indices && selectedIndex != index) index else null
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val chartHeight = size.height - 30.dp.toPx()
                val slotWidth = size.width / items.size
                val barWidth = (slotWidth * 0.45f).coerceAtMost(28.dp.toPx())

                // Draw 3 dashed horizontal gridlines
                val gridStep = chartHeight / 3
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                for (i in 0..2) {
                    val y = gridStep * i + 10.dp.toPx()
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }

                // Draw bars and labels
                items.forEachIndexed { index, item ->
                    val centerX = slotWidth * index + slotWidth / 2f
                    val left = centerX - barWidth / 2f

                    // Draw Planned Bar (behind / stacked background)
                    val plannedHeight = (item.planned / maxVal) * chartHeight * progress.value
                    val plannedTop = chartHeight - plannedHeight + 10.dp.toPx()

                    drawRoundRect(
                        color = mutedColor,
                        topLeft = Offset(left, plannedTop),
                        size = Size(barWidth, plannedHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Actual Bar (accent color in front)
                    val actualHeight = (item.actual / maxVal) * chartHeight * progress.value
                    val actualTop = chartHeight - actualHeight + 10.dp.toPx()

                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(left, actualTop),
                        size = Size(barWidth, actualHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Bottom Label
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(
                                (textSecondary.alpha * 255).toInt(),
                                (textSecondary.red * 255).toInt(),
                                (textSecondary.green * 255).toInt(),
                                (textSecondary.blue * 255).toInt()
                            )
                            textSize = 11.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            item.label,
                            centerX,
                            size.height - 6.dp.toPx(),
                            paint
                        )
                    }

                    // Draw Tooltip if selected
                    if (selectedIndex == index) {
                        val bubbleWidth = 64.dp.toPx()
                        val bubbleHeight = 28.dp.toPx()
                        val bubbleTop = (actualTop - bubbleHeight - 8.dp.toPx()).coerceAtLeast(0f)
                        val bubbleLeft = (centerX - bubbleWidth / 2f).coerceIn(4.dp.toPx(), size.width - bubbleWidth - 4.dp.toPx())

                        // White rounded bubble
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(bubbleLeft, bubbleTop),
                            size = Size(bubbleWidth, bubbleHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Small caret pointing down
                        val caretPath = Path().apply {
                            moveTo(centerX - 5.dp.toPx(), bubbleTop + bubbleHeight)
                            lineTo(centerX + 5.dp.toPx(), bubbleTop + bubbleHeight)
                            lineTo(centerX, bubbleTop + bubbleHeight + 5.dp.toPx())
                            close()
                        }
                        drawPath(caretPath, color = Color.White)

                        // Dark text in tooltip
                        drawIntoCanvas { canvas ->
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 11.sp.toPx()
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText(
                                item.tooltipText,
                                bubbleLeft + bubbleWidth / 2f,
                                bubbleTop + bubbleHeight / 2f + 4.dp.toPx(),
                                textPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
