package com.auraplay.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.auraplay.player.ui.theme.Background
import com.auraplay.player.ui.theme.Primary
import com.auraplay.player.ui.theme.Secondary
import com.auraplay.player.ui.theme.Tertiary
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WinampVisualizer(
    isPlaying: Boolean,
    progress: Long,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "visualizer")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF06060B), Background, Color(0xFF101726))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barCount = 48
            val gap = 3.dp.toPx()
            val barWidth = (size.width - gap * (barCount - 1)) / barCount
            val time = if (isPlaying) phase + (progress / 220f) else 0.6f
            val baseLine = size.height * 0.86f

            for (i in 0 until barCount) {
                val waveA = sin(time + i * 0.42f)
                val waveB = sin(time * 0.63f + i * 0.19f)
                val peak = ((waveA + waveB + 2f) / 4f).coerceIn(0f, 1f)
                val idle = if (isPlaying) 1f else 0.18f
                val h = (size.height * (0.14f + peak * 0.72f) * idle).coerceAtLeast(4.dp.toPx())
                val x = i * (barWidth + gap)
                val color = when {
                    peak > 0.74f -> Tertiary
                    peak > 0.48f -> Secondary
                    else -> Primary
                }

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.98f), color.copy(alpha = 0.28f)),
                        startY = baseLine - h,
                        endY = baseLine
                    ),
                    topLeft = Offset(x, baseLine - h),
                    size = Size(barWidth, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            val oscColor = Primary.copy(alpha = if (isPlaying) 0.62f else 0.22f)
            val points = 96
            var previous: Offset? = null
            for (i in 0..points) {
                val x = size.width * i / points
                val y = size.height * 0.34f + sin(time * 1.7f + i * 0.22f) * size.height * 0.11f
                val current = Offset(x, y)
                previous?.let { drawLine(oscColor, it, current, strokeWidth = 2.dp.toPx()) }
                previous = current
            }

            drawRect(
                color = Color.White.copy(alpha = 0.05f),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
