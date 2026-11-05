package com.onus.demotest.feature.skin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SkinPage { HOME, SECONDARY }

/** In-Activity routes only; stores route names instead of View or Activity references. */
class LocaleSkinPageRouter(restoredStack: List<String>? = null) {
    private val stack = mutableListOf(SkinPage.HOME).apply {
        if (restoredStack == listOf(SkinPage.HOME.name, SkinPage.SECONDARY.name)) {
            add(SkinPage.SECONDARY)
        }
    }
    private val mutableCurrent = MutableStateFlow(stack.last())
    val current = mutableCurrent.asStateFlow()
    val canGoBack: Boolean get() = stack.size > 1

    fun openSecondary() {
        if (stack.last() == SkinPage.SECONDARY) return
        stack.add(SkinPage.SECONDARY)
        mutableCurrent.value = stack.last()
    }

    fun back() {
        if (!canGoBack) return
        stack.removeAt(stack.lastIndex)
        mutableCurrent.value = stack.last()
    }

    fun snapshot(): List<String> = stack.map { it.name }
}
