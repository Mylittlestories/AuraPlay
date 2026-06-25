package com.auraplay.player.audio

import android.os.CountDownTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepTimer @Inject constructor() {

    private var timer: CountDownTimer? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    private val _totalMs = MutableStateFlow(0L)
    val totalMs: StateFlow<Long> = _totalMs.asStateFlow()

    var onTimerFinished: (() -> Unit)? = null

    fun start(durationMs: Long) {
        cancel()
        _totalMs.value = durationMs
        _isRunning.value = true

        timer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingMs.value = millisUntilFinished
            }

            override fun onFinish() {
                _isRunning.value = false
                _remainingMs.value = 0
                onTimerFinished?.invoke()
            }
        }.start()
    }

    fun cancel() {
        timer?.cancel()
        timer = null
        _isRunning.value = false
        _remainingMs.value = 0
    }

    fun addTime(durationMs: Long) {
        val currentRemaining = _remainingMs.value
        if (_isRunning.value) {
            start(currentRemaining + durationMs)
        } else {
            start(durationMs)
        }
    }

    val formattedRemaining: String
        get() {
            val ms = _remainingMs.value
            val totalSeconds = ms / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
            else "%02d:%02d".format(minutes, seconds)
        }
}