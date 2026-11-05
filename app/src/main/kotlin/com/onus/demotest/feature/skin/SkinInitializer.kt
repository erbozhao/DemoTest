package com.onus.demotest.feature.skin

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.Initializer

/** Restore the app-wide default before any Activity is created. */
class SkinInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AppCompatDelegate.setDefaultNightMode(LocaleSkinSettings(context).readNightMode())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
