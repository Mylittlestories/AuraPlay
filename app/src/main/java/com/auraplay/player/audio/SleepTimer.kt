package com.auraplay.player.audio
import android.os.CountDownTimer
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepTimer @Inject constructor() {
    private var timer: CountDownTimer? = null
    val running = MutableStateFlow(false)
    val remaining = MutableStateFlow(0L)
    var onFinish: (() -> Unit)? = null
    fun start(ms: Long) {
        cancel(); running.value = true
        timer = object : CountDownTimer(ms, 1000) {
            override fun onTick(left: Long) { remaining.value = left }
            override fun onFinish() { running.value = false; remaining.value = 0; onFinish?.invoke() }
        }.start()
    }
    fun cancel() { timer?.cancel(); timer = null; running.value = false; remaining.value = 0 }
    fun formatted(): String { val s = remaining.value / 1000; return "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60) }
}
