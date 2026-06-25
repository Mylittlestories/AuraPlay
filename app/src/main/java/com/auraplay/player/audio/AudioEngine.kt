package com.auraplay.player.audio
import android.content.Context
import android.media.audiofx.*
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqState(val enabled: Boolean = false, val bands: List<Short> = emptyList(), val preset: Int = 0)
data class FxState(val enabled: Boolean = false, val strength: Short = 0)
data class LoudState(val enabled: Boolean = false, val gain: Int = 0)

@Singleton
class AudioEngine @Inject constructor(@ApplicationContext private val ctx: Context) {
    private var eq: Equalizer? = null
    private var bb: BassBoost? = null
    private var vz: Virtualizer? = null
    private var le: LoudnessEnhancer? = null
    val eqState = MutableStateFlow(EqState())
    val bbState = MutableStateFlow(FxState())
    val vzState = MutableStateFlow(FxState())
    val leState = MutableStateFlow(LoudState())

    fun init(sid: Int) {
        release()
        try { eq = Equalizer(0, sid).apply { enabled = true } } catch (_: Exception) {}
        try { bb = BassBoost(0, sid).apply { enabled = true } } catch (_: Exception) {}
        try { vz = Virtualizer(0, sid).apply { enabled = true } } catch (_: Exception) {}
        try { if (Build.VERSION.SDK_INT >= 28) le = LoudnessEnhancer(sid).apply { enabled = true } } catch (_: Exception) {}
        sync()
    }

    fun bandCount() = eq?.numberOfBands?.toInt() ?: 0
    fun bandFreqs() = (0 until bandCount()).map { eq!!.getCenterFreq(it.toShort()) / 1000 }

    fun setBand(b: Short, l: Short) { eq?.let { it.setBandLevel(b, l.coerceIn(it.bandLevelRange[0], it.bandLevelRange[1])); syncEq() } }
    fun setPreset(p: Short) { eq?.usePreset(p); syncEq() }
    fun setBbStrength(s: Short) { bb?.setStrength(s); syncBb() }
    fun setVzStrength(s: Short) { vz?.setStrength(s); syncVz() }
    fun setLoudGain(g: Int) { if (Build.VERSION.SDK_INT >= 28) le?.setTargetGain(g); syncLe() }

    fun eqEnabled(e: Boolean) { eq?.enabled = e; syncEq() }
    fun bbEnabled(e: Boolean) { bb?.enabled = e; syncBb() }
    fun vzEnabled(e: Boolean) { vz?.enabled = e; syncVz() }
    fun leEnabled(e: Boolean) { le?.enabled = e; syncLe() }

    fun applyCurve(levels: List<Int>, bbS: Int = 0, vzS: Int = 0, lG: Int = 0) {
        eq?.let { e -> levels.forEachIndexed { i, v -> if (i < e.numberOfBands) e.setBandLevel(i.toShort(), v.toShort().coerceIn(e.bandLevelRange[0], e.bandLevelRange[1])) } }
        bb?.setStrength(bbS.toShort().coerceAtMost(1000))
        vz?.setStrength(vzS.toShort().coerceAtMost(1000))
        if (Build.VERSION.SDK_INT >= 28) le?.setTargetGain(lG)
        eq?.enabled = true; bb?.enabled = true; vz?.enabled = true; le?.enabled = true
        sync()
    }

    private fun sync() { syncEq(); syncBb(); syncVz(); syncLe() }
    private fun syncEq() { eq?.let { e -> eqState.value = EqState(e.enabled, (0 until e.numberOfBands).map { e.getBandLevel(it.toShort()) }, 0) } }
    private fun syncBb() { bb?.let { bbState.value = FxState(it.enabled, it.roundedStrength) } }
    private fun syncVz() { vz?.let { vzState.value = FxState(it.enabled, it.roundedStrength) } }
    private fun syncLe() { le?.let { if (Build.VERSION.SDK_INT >= 28) leState.value = LoudState(it.enabled, it.targetGain.toInt()) } }

    fun release() {
        try { eq?.release() } catch (_: Exception) {}
        try { bb?.release() } catch (_: Exception) {}
        try { vz?.release() } catch (_: Exception) {}
        try { le?.release() } catch (_: Exception) {}
        eq = null; bb = null; vz = null; le = null
    }
}
