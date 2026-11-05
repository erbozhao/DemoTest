package com.onus.demotest.feature.webview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.SparseArray
import android.view.View
import android.view.ViewStructure
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.view.inputmethod.InputMethodManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.onus.demotest.R

/** Standard WebView: website-owned login, without an explicit Autofill commit. */
class PasswordWebsiteActivity : AppCompatActivity() {
    private var allowsAutofill = true
    private lateinit var container: LinearLayout
    private val hintRes: Int
        get() = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> R.string.password_demo_status_unsupported
            allowsAutofill -> R.string.password_demo_website_hint
            else -> R.string.password_demo_website_no_autofill_hint
        }
    private lateinit var webView: WebView
    private lateinit var address: AppCompatAutoCompleteTextView
    private lateinit var status: TextView
    private var pageFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        allowsAutofill = savedInstanceState?.getBoolean("allowsAutofill", true) ?: true
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            if (!allowsAutofill && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            }
        }
        setContentView(container)
        ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(container)
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(12))
        }
        container.addView(controls)
        val actions = LinearLayout(this).apply {
            isBaselineAligned = false
            gravity = android.view.Gravity.CENTER_VERTICAL
        }.also { controls.addView(it) }
        fun addButton(label: Int, action: () -> Unit) {
            actions.addView(styledButton(label).apply {
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                if (actions.childCount > 0) marginStart = dp(6)
            })
        }
        addButton(R.string.password_demo_back) { navigateBack() }
        addButton(R.string.password_demo_refresh) { webView.reload() }
        addButton(R.string.password_demo_test_site) { loadAddress(DEFAULT_URL) }
        addButton(R.string.password_demo_settings) { openAutofillSettings() }
        controls.addView(TextView(this).apply {
            setText(R.string.password_demo_intro)
            setTextColor(Color.DKGRAY)
            textSize = 13f
            setPadding(0, dp(12), 0, dp(8))
        })
        val sites = RECOMMENDED_SITES
        val recommendations = object : ArrayAdapter<RecommendedSite>(
            this, android.R.layout.simple_dropdown_item_1line, sites
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                ((convertView as? TextView) ?: TextView(context)).apply {
                    text = getItem(position).toString()
                    layoutParams = android.widget.AbsListView.LayoutParams(-1, -2)
                    setTextColor(Color.BLACK)
                    textSize = 13f
                    setSingleLine(false)
                    maxLines = 4
                    ellipsize = null
                    setHorizontallyScrolling(false)
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                }
        }
        address = AppCompatAutoCompleteTextView(this).apply {
            setTextColor(Color.BLACK)
            setHintTextColor(Color.DKGRAY)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine()
            textSize = 13f
            setPadding(dp(10), 0, dp(10), 0)
            setBackgroundColor(Color.TRANSPARENT)
            setAdapter(recommendations)
            threshold = 1
            setDropDownBackgroundDrawable(roundedBackground(Color.WHITE, Color.rgb(215, 220, 226)))
            setOnItemClickListener { parent, _, position, _ ->
                val site = parent.getItemAtPosition(position) as RecommendedSite
                setText(site.url, false)
                setSelection(text.length)
                dismissDropDown()
            }
            hint = getString(R.string.password_demo_address)
            // This is a URL bar, not a login field.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            }
            isSaveEnabled = false
        }
        status = TextView(this).apply {
            setTextColor(Color.DKGRAY)
            setText(hintRes)
            textSize = 12f
        }
        controls.addView(SwitchCompat(this).apply {
            setText(R.string.password_demo_autofill_switch)
            setTextColor(Color.BLACK)
            textSize = 15f
            minHeight = dp(48)
            isSaveEnabled = false
            isChecked = allowsAutofill
            isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            setOnCheckedChangeListener { _, checked ->
                val currentUrl = webView.url?.takeIf { isHttps(it.toUri()) } ?: DEFAULT_URL
                cancelAutofill()
                destroyWebView()
                allowsAutofill = checked
                createWebView()
                loadAddress(currentUrl)
            }
        })
        controls.addView(status)
        controls.addView(TextView(this).apply {
            setText(R.string.password_demo_switch_note)
            setTextColor(Color.GRAY)
            textSize = 11f
            setPadding(0, dp(4), 0, dp(12))
        })
        val navigation = LinearLayout(this).apply {
            gravity = android.view.Gravity.CENTER_VERTICAL
        }.also { controls.addView(it) }
        val addressBox = LinearLayout(this).apply {
            id = View.generateViewId()
            gravity = android.view.Gravity.CENTER_VERTICAL
            background = roundedBackground(Color.rgb(247, 248, 250), Color.rgb(215, 220, 226))
        }
        addressBox.addView(address, LinearLayout.LayoutParams(0, -1, 1f))
        address.dropDownAnchor = addressBox.id
        addressBox.addView(Button(this).apply {
            text = "▾"
            textSize = 18f
            setTextColor(Color.DKGRAY)
            minWidth = 0
            minimumWidth = 0
            setPadding(0, 0, 0, 0)
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = getString(R.string.password_demo_recommended_sites)
            setOnClickListener {
                address.showSoftInputOnFocus = false
                address.requestFocus()
                getSystemService(InputMethodManager::class.java)
                    ?.hideSoftInputFromWindow(address.windowToken, 0)
                // Always offer the full list, including when the current URL is already entered.
                recommendations.filter.filter(null) {
                    address.showDropDown()
                    address.showSoftInputOnFocus = true
                }
            }
        }, LinearLayout.LayoutParams(dp(32), -1))
        navigation.addView(addressBox, LinearLayout.LayoutParams(0, dp(44), 1f))
        navigation.addView(styledButton(R.string.password_demo_load).apply {
            setOnClickListener { loadAddress(address.text.toString().trim()) }
        }, LinearLayout.LayoutParams(dp(64), dp(44)).apply { marginStart = dp(8) })
        container.addView(View(this).apply { setBackgroundColor(Color.rgb(224, 228, 233)) },
            LinearLayout.LayoutParams(-1, dp(1)))
        createWebView()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = navigateBack()
        })
        // Save only the current HTTPS URL, never form values or WebView state.
        loadAddress(savedInstanceState?.getString("websiteUrl") ?: DEFAULT_URL)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun roundedBackground(fill: Int, stroke: Int) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(8).toFloat()
        setStroke(dp(1), stroke)
    }

    private fun styledButton(label: Int) = Button(this).apply {
        setText(label)
        textSize = 12f
        isAllCaps = false
        setTextColor(Color.rgb(45, 55, 68))
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(4), 0, dp(4), 0)
        backgroundTintList = null
        background = roundedBackground(Color.rgb(242, 245, 248), Color.rgb(220, 225, 231))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView() {
        val autofillEnabled = allowsAutofill
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            container.importantForAutofill = if (autofillEnabled) {
                View.IMPORTANT_FOR_AUTOFILL_AUTO
            } else {
                View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            }
        }
        status.setText(hintRes)
        webView = object : WebView(this) {
            // Android 14+ may include even unimportant views in Autofill structures.
            // The disabled test also withholds virtual HTML fields and rejects filling them.
            override fun onProvideAutofillVirtualStructure(structure: ViewStructure, flags: Int) {
                if (autofillEnabled) super.onProvideAutofillVirtualStructure(structure, flags)
            }

            override fun autofill(values: SparseArray<AutofillValue>) {
                if (autofillEnabled) super.autofill(values)
            }
        }.apply {
            isSaveEnabled = false
            if (!allowsAutofill && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            }
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (isHttps(request.url)) return false
                    status.setText(R.string.password_demo_https_only)
                    return true
                }

                override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                    // Reflect redirects as well, so the visible address always identifies the current site.
                    address.setText(url, false)
                    pageFailed = false
                    status.setText(R.string.password_demo_loading)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    address.setText(url, false)
                    if (pageFailed || view.url != url) return
                    status.setText(hintRes)
                }

                override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                    if (request.isForMainFrame) {
                        pageFailed = true
                        status.setText(R.string.password_demo_load_failed)
                    }
                }

                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame) {
                        pageFailed = true
                        status.setText(R.string.password_demo_load_failed)
                    }
                }
            }
        }
        container.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun loadAddress(value: String) {
        val uri = value.toUri()
        if (!isHttps(uri)) {
            status.setText(R.string.password_demo_https_only)
            return
        }
        address.dismissDropDown()
        address.clearFocus()
        getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(address.windowToken, 0)
        webView.loadUrl(uri.toString())
    }

    private fun isHttps(uri: Uri) = uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null

    private fun navigateBack() {
        if (webView.canGoBack()) webView.goBack() else finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("allowsAutofill", allowsAutofill)
        webView.url?.let { if (isHttps(it.toUri())) outState.putString("websiteUrl", it) }
        super.onSaveInstanceState(outState)
    }

    private fun openAutofillSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE, "package:$packageName".toUri())
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, R.string.password_demo_settings_unavailable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelAutofill() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(AutofillManager::class.java)?.cancel()
        }
    }

    private fun destroyWebView() {
        (webView.parent as? LinearLayout)?.removeView(webView)
        webView.stopLoading()
        webView.removeAllViews()
        webView.destroy()
    }

    override fun onDestroy() {
        cancelAutofill()
        destroyWebView()
        super.onDestroy()
    }

    private data class RecommendedSite(val name: String, val url: String) {
        override fun toString() = "$name\n$url"
    }

    companion object {
        private const val DEFAULT_URL = "https://the-internet.herokuapp.com/login"
        private val RECOMMENDED_SITES = listOf(
            RecommendedSite("The Internet · 登录测试", DEFAULT_URL),
            RecommendedSite("Authentication Test · 表单登录",
                "https://authenticationtest.com/simpleFormAuth/"),
            RecommendedSite("Practice Test Automation · 登录测试",
                "https://practicetestautomation.com/practice-test-login/"),
            RecommendedSite("Authentication Test · 分步登录",
                "https://authenticationtest.com/multiStepAuth/")
        )
    }
}
