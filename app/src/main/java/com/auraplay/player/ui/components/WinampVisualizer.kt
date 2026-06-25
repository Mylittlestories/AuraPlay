package com.auraplay.player.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Winamp-style spectrum analyzer visualizer
 * Simulates audio visualization with animated bars
 */
@Composable
fun WinampSpectrumVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    barColor: Color = Color(0xFF00FF00),
    peakColor: Color = Color(0xFFFFFF00),
    backgroundColor: Color = Color(0xFF0A0A0A)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "viz")

    // Animated values for each bar
    val animatedValues = remember { MutableList(barCount) { Animatable(0f) } }
    val peakValues = remember { MutableList(barCount) { Animatable(0f) } }

    // Animate bars when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animatedValues.forEachIndexed { index, animatable ->
                launch {
                    while (true) {
                        val target = (sin(System.currentTimeMillis() / (200.0 + index * 30)) * 0.4 + 0.5).toFloat()
                            .coerceIn(0.1f, 1.0f) * (0.5f + Random.nextFloat() * 0.5f)
                        animatable.animateTo(
                            targetValue = target,
                            animationSpec = tween(durationMillis = 100 + Random.nextInt(100))
                        )
                    }
                }
            }
            peakValues.forEachIndexed { index, animatable ->
                launch {
                    while (true) {
                        val target = (sin(System.currentTimeMillis() / (300.0 + index * 40)) * 0.3 + 0.6).toFloat()
                            .coerceIn(0.2f, 1.0f)
                        animatable.animateTo(
                            targetValue = target,
                            animationSpec = tween(durationMillis = 200)
                        )
                        kotlinx.coroutines.delay(500 + Random.nextLong(1000))
                    }
                }
            }
        } else {
            animatedValues.forEach { it.animateTo(0f, animationSpec = tween(500)) }
            peakValues.forEach { it.animateTo(0f, animationSpec = tween(800)) }
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        val barWidth = size.width / (barCount * 1.5f)
        val gap = barWidth * 0.5f
        val maxHeight = size.height

        // Background
        drawRect(color = backgroundColor, size = size)

        // Draw bars
        animatedValues.forEachIndexed { index, animatable ->
            val x = index * (barWidth + gap) + gap
            val barHeight = maxHeight * animatable.value
            val y = maxHeight - barHeight

            // Bar gradient (green to yellow to red)
            val barBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF0000),
                    Color(0xFFFF6600),
                    Color(0xFFFFFF00),
                    barColor
                ),
                startY = y,
                endY = maxHeight
            )

            // Draw bar segments (Winamp style)
            val segmentHeight = 3.dp.toPx()
            val segmentGap = 1.dp.toPx()
            var currentY = maxHeight

            while (currentY > y) {
                val segmentTop = maxOf(currentY - segmentHeight, y)
                drawRect(
                    color = if (currentY < maxHeight * 0.3f) Color(0xFFFF0000)
                    else if (currentY < maxHeight * 0.6f) Color(0xFFFFFF00)
                    else barColor,
                    topLeft = Offset(x, segmentTop),
                    size = Size(barWidth, segmentHeight - segmentGap)
                )
                currentY -= segmentHeight + segmentGap
            }

            // Peak indicator
            val peakY = maxHeight - (maxHeight * peakValues[index].value)
            drawRect(
                color = peakColor,
                topLeft = Offset(x, peakY - 2.dp.toPx()),
                size = Size(barWidth, 2.dp.toPx())
            )
        }
    }
}

/**
 * Winamp-style oscilloscope waveform
 */
@Composable
fun WinampWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    waveColor: Color = Color(0xFF00FF00)
) {
    val phase = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                phase.animateTo(
                    targetValue = phase.value + 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(40.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        drawRect(color = Color(0xFF0A0A0A), size = size)

        if (isPlaying) {
            val points = mutableListOf<Offset>()
            var x = 0f
            while (x < width) {
                val normalizedX = x / width
                val y = centerY + sin(normalizedX * 12 + phase.value * 2) * height * 0.3f *
                        sin(normalizedX * 3.14f) * (0.5f + Random.nextFloat() * 0.5f)
                points.add(Offset(x, y))
                x += 2f
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = waveColor.copy(alpha = 0.8f),
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Winamp-style scrolling title ticker
 */
@Composable
fun WinampTicker(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF00FF00)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    val offset by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker_offset"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(20.dp)) {
        val textWidth = text.length * 12f
        val x = (size.width + textWidth) * ((offset + 1) / 2) - textWidth

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = textColor.hashCode()
                textSize = 14.dp.toPx()
                isAntiAlias = true
                typeface = android.graphics.Typeface.MONOSPACE
            }
            drawText(text, x, size.height * 0.8f, paint)
        }
    }
}