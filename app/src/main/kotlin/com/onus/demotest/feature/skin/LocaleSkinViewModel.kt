package com.onus.demotest.feature.skin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Requested settings; resolved strings/colors/images belong to the View's current resources. */
data class LocaleSkinUiState(val languageTags: String, val nightMode: Int)

class LocaleSkinViewModel(
    private val settings: LocaleSkinSettingsStore,
    initialLanguageTags: String
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        LocaleSkinUiState(initialLanguageTags, settings.readNightMode())
    )
    val state = mutableState.asStateFlow()

    /** Refresh retained page state without writing settings or issuing a switch. */
    fun syncFromApp(languageTags: String) {
        mutableState.value = LocaleSkinUiState(languageTags, settings.readNightMode())
    }

    fun selectLanguage(tags: String) {
        require(tags in setOf("", "zh", "en"))
        mutableState.value = mutableState.value.copy(languageTags = tags)
    }

    fun selectNightMode(mode: Int) {
        if (mode == mutableState.value.nightMode) return
        settings.saveNightMode(mode)
        mutableState.value = mutableState.value.copy(nightMode = mode)
    }
}
