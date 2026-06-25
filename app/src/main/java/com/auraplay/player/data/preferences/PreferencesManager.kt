package com.auraplay.player.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "auraplay_settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val SHUFFLE_MODE = stringPreferencesKey("shuffle_mode")
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val AMP_MODE = stringPreferencesKey("amp_mode")
        val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val GAPLESS_ENABLED = booleanPreferencesKey("gapless_enabled")
        val SLEEP_TIMER_MINUTES = intPreferencesKey("sleep_timer_minutes")
        val LAST_PLAYED_TRACK_ID = longPreferencesKey("last_played_track_id")
        val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
        val AUTO_SCAN = booleanPreferencesKey("auto_scan")
        val SHOW_LOCK_SCREEN_CONTROLS = booleanPreferencesKey("lock_screen_controls")
        val VOLUME_NORMALIZATION = booleanPreferencesKey("volume_normalization")
        val REPLAY_GAIN = floatPreferencesKey("replay_gain")
    }

    // Theme
    val themeFlow: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "AURAPLAY" }
    suspend fun setTheme(theme: String) { context.dataStore.edit { it[Keys.THEME] = theme } }

    // Shuffle
    val shuffleModeFlow: Flow<String> = context.dataStore.data.map { it[Keys.SHUFFLE_MODE] ?: "SMART" }
    suspend fun setShuffleMode(mode: String) { context.dataStore.edit { it[Keys.SHUFFLE_MODE] = mode } }

    // Repeat
    val repeatModeFlow: Flow<String> = context.dataStore.data.map { it[Keys.REPEAT_MODE] ?: "OFF" }
    suspend fun setRepeatMode(mode: String) { context.dataStore.edit { it[Keys.REPEAT_MODE] = mode } }

    // Playback Speed
    val playbackSpeedFlow: Flow<Float> = context.dataStore.data.map { it[Keys.PLAYBACK_SPEED] ?: 1.0f }
    suspend fun setPlaybackSpeed(speed: Float) { context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed } }

    // Amp Mode
    val ampModeFlow: Flow<String> = context.dataStore.data.map { it[Keys.AMP_MODE] ?: "OFF" }
    suspend fun setAmpMode(mode: String) { context.dataStore.edit { it[Keys.AMP_MODE] = mode } }

    // Crossfade
    val crossfadeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[Keys.CROSSFADE_ENABLED] ?: false }
    suspend fun setCrossfadeEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.CROSSFADE_ENABLED] = enabled } }

    val crossfadeDurationFlow: Flow<Int> = context.dataStore.data.map { it[Keys.CROSSFADE_DURATION] ?: 3000 }
    suspend fun setCrossfadeDuration(ms: Int) { context.dataStore.edit { it[Keys.CROSSFADE_DURATION] = ms } }

    // Gapless
    val gaplessEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[Keys.GAPLESS_ENABLED] ?: true }
    suspend fun setGaplessEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.GAPLESS_ENABLED] = enabled } }

    // Last Played
    val lastPlayedTrackIdFlow: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_PLAYED_TRACK_ID] ?: -1L }
    suspend fun setLastPlayedTrackId(id: Long) { context.dataStore.edit { it[Keys.LAST_PLAYED_TRACK_ID] = id } }

    val lastPlayedPositionFlow: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_PLAYED_POSITION] ?: 0L }
    suspend fun setLastPlayedPosition(pos: Long) { context.dataStore.edit { it[Keys.LAST_PLAYED_POSITION] = pos } }

    // Auto Scan
    val autoScanFlow: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_SCAN] ?: true }
    suspend fun setAutoScan(enabled: Boolean) { context.dataStore.edit { it[Keys.AUTO_SCAN] = enabled } }

    // Lock Screen Controls
    val lockScreenControlsFlow: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_LOCK_SCREEN_CONTROLS] ?: true }
    suspend fun setLockScreenControls(enabled: Boolean) { context.dataStore.edit { it[Keys.SHOW_LOCK_SCREEN_CONTROLS] = enabled } }

    // Volume Normalization
    val volumeNormalizationFlow: Flow<Boolean> = context.dataStore.data.map { it[Keys.VOLUME_NORMALIZATION] ?: false }
    suspend fun setVolumeNormalization(enabled: Boolean) { context.dataStore.edit { it[Keys.VOLUME_NORMALIZATION] = enabled } }

    // Replay Gain
    val replayGainFlow: Flow<Float> = context.dataStore.data.map { it[Keys.REPLAY_GAIN] ?: 0f }
    suspend fun setReplayGain(gain: Float) { context.dataStore.edit { it[Keys.REPLAY_GAIN] = gain } }
}