package com.auraplay.player.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WinampSpectrumVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    barColor: Color = Color(0xFF00FF00),
    peakColor: Color = Color(0xFFFFFF00),
    backgroundColor: Color = Color(0xFF0A0A0A)
) {
    // Simulated bar heights driven by time
    var frameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                frameTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(50)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        val barWidth = size.width / (barCount * 1.5f)
        val gap = barWidth * 0.5f
        val maxHeight = size.height

        drawRect(color = backgroundColor, size = size)

        for (index in 0 until barCount) {
            val x = index * (barWidth + gap) + gap

            // Simulate bar heights with sine waves
            val barHeight = if (isPlaying) {
                val t = frameTime / 1000.0
                val value = (sin(t * 3 + index * 0.5) * 0.3 + 0.5) *
                        (sin(t * 1.7 + index * 0.3) * 0.2 + 0.6) *
                        (0.4 + Random.nextFloat() * 0.6)
                (maxHeight * value.toFloat().coerceIn(0.1f, 1.0f))
            } else 0f

            val y = maxHeight - barHeight

            // Draw segmented bars (Winamp style)
            val segHeight = 3.dp.toPx()
            val segGap = 1.dp.toPx()
            var currentY = maxHeight

            while (currentY > y) {
                val segTop = maxOf(currentY - segHeight, y)
                val fraction = 1f - (segTop / maxHeight)
                val color = when {
                    fraction > 0.8f -> Color(0xFFFF0000)
                    fraction > 0.5f -> Color(0xFFFFFF00)
                    else -> barColor
                }
                drawRect(
                    color = color,
                    topLeft = Offset(x, segTop),
                    size = Size(barWidth, segHeight - segGap)
                )
                currentY -= segHeight + segGap
            }

            // Peak indicator
            if (isPlaying) {
                val peakY = y - 2.dp.toPx()
                drawRect(
                    color = peakColor,
                    topLeft = Offset(x, peakY.coerceAtLeast(0f)),
                    size = Size(barWidth, 2.dp.toPx())
                )
            }
        }
    }
}