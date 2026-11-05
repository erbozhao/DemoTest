package com.onus.demotest.feature.skin

import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleSkinViewModelTest {
    @Test
    fun languageAndAppearanceSelectionsRemainIndependent() {
        val settings = FakeSettings(-1)
        val viewModel = LocaleSkinViewModel(settings, "zh")
        viewModel.selectLanguage("en")
        viewModel.selectNightMode(2)
        assertEquals(LocaleSkinUiState("en", 2), viewModel.state.value)
        viewModel.selectLanguage("")
        assertEquals(LocaleSkinUiState("", 2), viewModel.state.value)
        assertEquals(1, settings.writes)
    }

    @Test
    fun reopeningRestoresAppearanceWithoutWritingAgain() {
        val settings = FakeSettings(-1)
        LocaleSkinViewModel(settings, "zh").selectNightMode(2)
        val reopened = LocaleSkinViewModel(settings, "en")
        assertEquals(LocaleSkinUiState("en", 2), reopened.state.value)
        reopened.selectNightMode(2)
        assertEquals(1, settings.writes)
    }

    @Test
    fun returningPageUsesOtherPagesSettingsWithoutOverwritingThem() {
        val settings = FakeSettings(-1)
        val firstPage = LocaleSkinViewModel(settings, "zh")
        val secondPage = LocaleSkinViewModel(settings, "zh")
        secondPage.selectLanguage("en")
        secondPage.selectNightMode(2)
        firstPage.syncFromApp(secondPage.state.value.languageTags)
        assertEquals(LocaleSkinUiState("en", 2), firstPage.state.value)
        assertEquals(1, settings.writes)
    }

    private class FakeSettings(private var nightMode: Int) : LocaleSkinSettingsStore {
        var writes = 0
        override fun readNightMode() = nightMode
        override fun saveNightMode(mode: Int) {
            nightMode = mode
            writes++
        }
    }
}
