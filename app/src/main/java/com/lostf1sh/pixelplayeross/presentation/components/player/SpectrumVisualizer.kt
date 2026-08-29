package com.lostf1sh.pixelplayeross.presentation.components.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.lostf1sh.pixelplayeross.data.service.player.AudiophileDspState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Live spectrum visualizer for the full player.
 *
 * Renders the post-DSP audio tapped by [AudiophileDspState] as a row of
 * log-spaced frequency bars tinted with the album palette — no
 * RECORD_AUDIO permission needed, because the tap lives inside the app's own
 * audio processor chain.
 */
@Composable
fun SpectrumVisualizerStrip(
    dspState: AudiophileDspState,
    isPlayingProvider: () -> Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 44
) {
    val window = remember { FloatArray(AudiophileDspState.TAP_SIZE) }
    // Real/imaginary scratch for the FFT.
    val re = remember { FloatArray(AudiophileDspState.TAP_SIZE) }
    val im = remember { FloatArray(AudiophileDspState.TAP_SIZE) }
    val smoothed = remember { FloatArray(barCount) }
    var levels by remember { mutableStateOf(FloatArray(barCount)) }
    var hasAudio by remember { mutableStateOf(false) }

    // Keep the tap running only while the strip is on screen.
    DisposableEffect(dspState) {
        dspState.tapActive = true
        onDispose {
            dspState.tapActive = false
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val playing = isPlayingProvider()
            if (playing && dspState.copyTap(window)) {
                hasAudio = true
                withContext(Dispatchers.Default) {
                    val fresh = analyzeSpectrum(window, re, im, barCount)
                    for (i in 0 until barCount) {
                        val target = fresh[i]
                        // Fast attack, slow release — classic VU ballistics.
                        smoothed[i] = if (target > smoothed[i]) {
                            target
                        } else {
                            smoothed[i] + (target - smoothed[i]) * 0.28f
                        }
                    }
                    levels = smoothed.copyOf()
                }
            } else if (hasAudio) {
                // No fresh audio: let the bars fall gently.
                withContext(Dispatchers.Default) {
                    var alive = false
                    for (i in 0 until barCount) {
                        smoothed[i] *= 0.90f
                        if (smoothed[i] > 0.01f) alive = true
                    }
                    levels = smoothed.copyOf()
                    if (!alive) hasAudio = false
                }
            }
            delay(33)
        }
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (hasAudio) 1f else 0.35f,
        animationSpec = tween(420),
        label = "VisualizerAlpha"
    )

    Canvas(modifier = modifier) {
        if (levels.isEmpty()) return@Canvas
        val gap = 2.dp.toPx()
        val barWidth = max(2.dp.toPx(), (size.width - gap * (barCount - 1)) / barCount)
        val barBrush = Brush.verticalGradient(
            colors = listOf(tint, tint.copy(alpha = 0.25f)),
            startY = 0f,
            endY = size.height
        )
        val radius = CornerRadius(barWidth / 2, barWidth / 2)

        for (i in 0 until barCount) {
            val level = (levels[i] * contentAlpha).coerceIn(0f, 1f)
            val barHeight = max(2.dp.toPx(), level * size.height)
            val left = i * (barWidth + gap)
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = radius
            )
        }

        // A soft outline showing the strip's baseline even when silent.
        drawLine(
            color = tint.copy(alpha = 0.22f * contentAlpha),
            start = Offset(0f, size.height - 1f),
            end = Offset(size.width, size.height - 1f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Computes [bars] log-spaced magnitudes (0..1) from a mono PCM window using a
 * simple radix-2 FFT. All buffers are caller-provided so the UI loop performs
 * no per-frame allocations.
 */
private fun analyzeSpectrum(
    window: FloatArray,
    re: FloatArray,
    im: FloatArray,
    bars: Int
): FloatArray {
    val n = window.size
    // Hann window + copy into the real part.
    for (i in 0 until n) {
        val w = 0.5f * (1f - kotlin.math.cos(2.0 * Math.PI * i / n).toFloat())
        re[i] = window[i] * w
        im[i] = 0f
    }
    fft(re, im)

    val output = FloatArray(bars)
    val halfBins = n / 2
    // Log spacing from ~40 Hz to ~16 kHz assuming a 48 kHz-ish rate.
    val minBin = max(2, (40 * n / 48_000f).toInt())
    val maxBin = min(halfBins - 1, (16_000 * n / 48_000f).toInt().coerceAtLeast(minBin + 1))
    val logMin = ln(minBin.toFloat())
    val logMax = ln(maxBin.toFloat())

    var peak = 1e-6f
    for (b in 0 until bars) {
        val from = (kotlin.math.exp(logMin + (logMax - logMin) * b / bars)).toInt()
        val to = max(from + 1, (kotlin.math.exp(logMin + (logMax - logMin) * (b + 1) / bars)).toInt())
        var sum = 0f
        var count = 0
        for (bin in from until to.coerceAtMost(halfBins)) {
            val mag = sqrt(re[bin] * re[bin] + im[bin] * im[bin])
            sum += mag
            count++
        }
        val avg = if (count > 0) sum / count else 0f
        // dB scale: -60 dB .. 0 dB mapped to 0 .. 1.
        val db = 20f * kotlin.math.log10(avg.coerceAtLeast(1e-7f))
        val norm = ((db + 60f) / 60f).coerceIn(0f, 1f)
        output[b] = norm
        if (norm > peak) peak = norm
    }
    // Gentle auto-gain so quiet recordings still move the bars.
    if (peak > 0.15f) {
        val boost = min(1.6f, 0.85f / peak)
        for (b in 0 until bars) output[b] = (output[b] * boost).coerceAtMost(1f)
    }
    return output
}

/** In-place iterative radix-2 FFT. Buffers must be a power of two. */
private fun fft(re: FloatArray, im: FloatArray) {
    val n = re.size
    // Bit reversal permutation.
    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while (j and bit != 0) {
            j = j xor bit
            bit = bit shr 1
        }
        j = j xor bit
        if (i < j) {
            val tmpRe = re[i]; re[i] = re[j]; re[j] = tmpRe
            val tmpIm = im[i]; im[i] = im[j]; im[j] = tmpIm
        }
    }
    // Butterflies.
    var len = 2
    while (len <= n) {
        val ang = (-2.0 * Math.PI / len).toFloat()
        val wLenRe = kotlin.math.cos(ang)
        val wLenIm = kotlin.math.sin(ang)
        var i = 0
        while (i < n) {
            var wRe = 1f
            var wIm = 0f
            val half = len / 2
            for (k in 0 until half) {
                val uRe = re[i + k]
                val uIm = im[i + k]
                val vRe = re[i + k + half] * wRe - im[i + k + half] * wIm
                val vIm = re[i + k + half] * wIm + im[i + k + half] * wRe
                re[i + k] = uRe + vRe
                im[i + k] = uIm + vIm
                re[i + k + half] = uRe - vRe
                im[i + k + half] = uIm - vIm
                val nextWRe = wRe * wLenRe - wIm * wLenIm
                wIm = wRe * wLenIm + wIm * wLenRe
                wRe = nextWRe
            }
            i += len
        }
        len = len shl 1
    }
}
