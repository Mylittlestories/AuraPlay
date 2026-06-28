package com.auraplay.player.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auraplay.player.ui.theme.AuraAppearance
import com.auraplay.player.ui.theme.AuraBackgroundPreset
import com.auraplay.player.ui.theme.AuraThemePreset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.auraSettingsDataStore by preferencesDataStore(name = "aura_settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_preset")
        val BACKGROUND = stringPreferencesKey("background_preset")
    }

    val appearance: Flow<AuraAppearance> = context.auraSettingsDataStore.data.map { prefs ->
        AuraAppearance(
            themePreset = prefs[Keys.THEME]?.let { value ->
                AuraThemePreset.entries.firstOrNull { it.name == value }
            } ?: AuraThemePreset.OCEAN,
            backgroundPreset = prefs[Keys.BACKGROUND]?.let { value ->
                AuraBackgroundPreset.entries.firstOrNull { it.name == value }
            } ?: AuraBackgroundPreset.DEEP
        )
    }

    suspend fun setThemePreset(preset: AuraThemePreset) {
        context.auraSettingsDataStore.edit { prefs -> prefs[Keys.THEME] = preset.name }
    }

    suspend fun setBackgroundPreset(preset: AuraBackgroundPreset) {
        context.auraSettingsDataStore.edit { prefs -> prefs[Keys.BACKGROUND] = preset.name }
    }
}
