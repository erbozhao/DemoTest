package com.onus.demotest.feature.skin

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

interface LocaleSkinSettingsStore {
    fun readNightMode(): Int
    fun saveNightMode(mode: Int)
}

/** Language persistence is handled by AppCompat's autoStoreLocales / the platform. */
class LocaleSkinSettings(context: Context) : LocaleSkinSettingsStore {
    private val preferences = context.applicationContext
        .getSharedPreferences("locale_skin_demo", Context.MODE_PRIVATE)

    override fun readNightMode(): Int = preferences.getInt(
        "night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    override fun saveNightMode(mode: Int) {
        preferences.edit().putInt("night_mode", mode).apply()
    }
}
