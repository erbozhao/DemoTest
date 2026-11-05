package com.onus.demotest.feature.skin

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import com.onus.demotest.R
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/** Connects UI state to AppCompat. Locale / uiMode changes recreate the resource-backed View. */
class LocaleSkinDemoActivity : AppCompatActivity() {
    private lateinit var router: LocaleSkinPageRouter

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("skin_page_stack", ArrayList(router.snapshot()))
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val settings = LocaleSkinSettings(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == LocaleSkinViewModel::class.java)
                @Suppress("UNCHECKED_CAST")
                return LocaleSkinViewModel(
                    settings, AppCompatDelegate.getApplicationLocales().toLanguageTags()
                ) as T
            }
        })[LocaleSkinViewModel::class.java]
        router = LocaleSkinPageRouter(savedInstanceState?.getStringArrayList("skin_page_stack"))
        val container = FrameLayout(this)
        setContentView(container)
        // Cached Page views belong only to this Activity instance. Recreation reloads all resources.
        val pages = mutableMapOf<SkinPage, LocaleSkinDemoView>()
        val backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() { router.back() }
        }
        onBackPressedDispatcher.addCallback(this, backCallback)
        fun showPage(page: SkinPage) {
            val demoView = pages.getOrPut(page) {
                val secondary = page == SkinPage.SECONDARY
                LocaleSkinDemoView(this,
                    titleRes = if (secondary) R.string.locale_skin_secondary_title else R.string.locale_skin_title,
                    navigationRes = if (secondary) R.string.locale_skin_back else R.string.locale_skin_open_secondary
                ).apply {
                    onNavigate = {
                        if (secondary) router.back() else router.openSecondary()
                    }
                    onLanguageSelected = viewModel::selectLanguage
                    onNightModeSelected = viewModel::selectNightMode
                }
            }
            demoView.render(viewModel.state.value)
            if (demoView.root.parent == null) {
                container.removeAllViews()
                container.addView(demoView.root)
            }
            backCallback.isEnabled = router.canGoBack
        }
        showPage(router.current.value)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Refresh global settings on resume before collecting retained state.
                viewModel.syncFromApp(AppCompatDelegate.getApplicationLocales().toLanguageTags())
                launch { router.current.collect { showPage(it) } }
                viewModel.state.collect { state ->
                    pages.values.forEach { it.render(state) }
                    // Avoid repeated configuration changes when collecting again after recreation.
                    val locales = LocaleListCompat.forLanguageTags(state.languageTags)
                    if (AppCompatDelegate.getApplicationLocales() != locales) {
                        AppCompatDelegate.setApplicationLocales(locales)
                    }
                    if (AppCompatDelegate.getDefaultNightMode() != state.nightMode) {
                        AppCompatDelegate.setDefaultNightMode(state.nightMode)
                    }
                }
            }
        }
    }
}
