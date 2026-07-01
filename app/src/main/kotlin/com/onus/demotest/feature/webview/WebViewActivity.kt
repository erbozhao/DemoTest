package com.onus.demotest.feature.webview

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.common.DeviceUtils

/**
 * @Author: onuszhao
 * @Date: 2023-09-08 17:00
 * @Description:
 */
class WebViewActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var refresh: TextView
    private lateinit var dark: TextView

    private lateinit var container:LinearLayout
    private lateinit var webView: WebView
    private lateinit var webViewSettings: WebSettings

    private var isNightMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@WebViewActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTheme(R.style.ActivityMain_Light)
        } else {
            setTheme(R.style.ActivityMain)
        }

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.color.white)
            container.addView(this, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }

        wrapper.addView(TextView(this).apply {
            refresh = this
            setBackgroundResource(R.color.theme_common_color_a2)
            text = "Refresh"
            textSize = resources.getDimension(R.dimen.dp_12)
            setTextColor(resources.getColor(R.color.black))
            val padding = resources.getDimensionPixelOffset(R.dimen.dp_8)
            setPaddingRelative(padding, padding, padding, padding)
            setOnClickListener(this@WebViewActivity)
        })

        wrapper.addView(TextView(this).apply {
            dark = this
            setBackgroundResource(R.color.theme_common_color_a2)
            text = "Dark Mode"
            textSize = resources.getDimension(R.dimen.dp_12)
            setTextColor(resources.getColor(R.color.black))
            val padding = resources.getDimensionPixelOffset(R.dimen.dp_8)
            setPaddingRelative(padding, padding, padding, padding)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = resources.getDimensionPixelOffset(R.dimen.dp_12)
            }
            setOnClickListener(this@WebViewActivity)
        })

        webView = WebView(applicationContext).apply {
            settings.apply {
                webViewSettings = this

                // settings.setWebContentsDebuggingEnabled(true)
                javaScriptEnabled = true
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                domStorageEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                savePassword = true
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportMultipleWindows(false)
                allowFileAccess = true
            }

            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }

        container.addView(webView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            weight = 1f
        })

        isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        setTheme(if (isNightMode) R.style.ActivityMain_DayNight else R.style.ActivityMain_Light)
        loadUrl()
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v) {
            refresh -> {
                loadUrl()
            }
            dark -> {
                refreshDarkModeIfNeed()
            }
        }
    }

    private fun loadUrl() {
        webView.loadUrl("https://www.baidu.com")
        // webView.loadUrl("https://m.youtube.com")
        // webView.loadUrl("https://www.google.com")
        // webView.loadUrl("https://www.netflix.com")
    }

    private fun refreshDarkModeIfNeed() {
        isNightMode = !isNightMode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            kotlin.runCatching {
                setTheme(if (isNightMode) R.style.ActivityMain_DayNight else R.style.ActivityMain_Light)
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(webViewSettings, isNightMode)
                } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(
                        webViewSettings,
                        if (isNightMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onuszhao", "WebViewActivity onDestroy")
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        webView.clearHistory()
        webView.removeAllViews()
        container.removeAllViews()
        webView.destroy()
        release()
    }

    override fun finish() {
        super.finish()
        Log.d("onuszhao", "WebViewActivity finish")
    }

    private fun release() {
        Log.d("onuszhao", "WebViewActivity release")
        this.moveTaskToBack(true)
        Thread {
            System.gc()
            // GcTrigger.runGc()
        }.start()
    }

}