package com.onus.demotest.feature.skin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LocaleSkinPageRouterTest {
    @Test
    fun recreationRestoresSecondPageAndBackStack() {
        val original = LocaleSkinPageRouter()
        original.openSecondary()
        original.openSecondary()
        val restored = LocaleSkinPageRouter(original.snapshot())
        assertEquals(SkinPage.SECONDARY, restored.current.value)
        restored.back()
        assertEquals(SkinPage.HOME, restored.current.value)
        assertFalse(restored.canGoBack)
    }

    @Test
    fun invalidSavedRoutesFallBackToHome() {
        val router = LocaleSkinPageRouter(listOf("removed-page"))
        assertEquals(SkinPage.HOME, router.current.value)
        router.back()
        assertEquals(listOf("HOME"), router.snapshot())
    }
}
