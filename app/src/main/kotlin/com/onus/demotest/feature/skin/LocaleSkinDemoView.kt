package com.onus.demotest.feature.skin

import androidx.appcompat.app.AppCompatDelegate
import android.content.res.Configuration
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.onus.demotest.R
import java.util.Locale
import kotlin.math.roundToInt

/** Builds the UI and renders state; delegates user actions to its owner. */
class LocaleSkinDemoView(
    private val context: Context,
    private val titleRes: Int,
    private val navigationRes: Int
) {
    val root: ScrollView
    private val languageGroup: RadioGroup
    private val themeGroup: RadioGroup
    private var rendering = false
    var onNavigate: () -> Unit = {}
    var onLanguageSelected: (String) -> Unit = {}
    var onNightModeSelected: (Int) -> Unit = {}
    private val resources get() = context.resources
    private val chineseId = View.generateViewId()
    private val englishId = View.generateViewId()
    private val systemLanguageId = View.generateViewId()
    private val lightId = View.generateViewId()
    private val darkId = View.generateViewId()
    private val systemThemeId = View.generateViewId()


    init {
        root = ScrollView(context).apply {
            isFillViewport = true
            setBackgroundResource(R.color.locale_skin_background)
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        root.addView(content, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        content.addText(titleRes, 24f)
        content.addView(Button(context).apply {
            setText(navigationRes)
            isAllCaps = false
            setOnClickListener { onNavigate() }
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(12) })
        content.addText(R.string.locale_skin_hint)
        content.addText(R.string.locale_skin_language, 18f)
        languageGroup = content.addOptions(
            chineseId to R.string.locale_skin_chinese,
            englishId to R.string.locale_skin_english,
            systemLanguageId to R.string.locale_skin_system
        )
        content.addText(R.string.locale_skin_theme, 18f)
        themeGroup = content.addOptions(
            lightId to R.string.locale_skin_light,
            darkId to R.string.locale_skin_dark,
            systemThemeId to R.string.locale_skin_system
        )
        content.addText(R.string.locale_skin_sample, 20f)
        val image = ImageView(context).apply {
            setImageResource(R.drawable.locale_skin_art)
            contentDescription = context.getString(R.string.locale_skin_image)
        }
        content.addView(image, LinearLayout.LayoutParams(dp(160), dp(160)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(12)
        })
        content.addText(R.string.locale_skin_image)
        content.addView(View(context).apply {
            setBackgroundResource(R.color.locale_skin_accent)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(24)).apply {
            bottomMargin = dp(12)
        })
        val status = content.addText(R.string.locale_skin_status, 14f).apply {
            setTextIsSelectable(true)
        }
        val isNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        val imageValue = TypedValue()
        resources.getValue(R.drawable.locale_skin_art, imageValue, true)
        status.text = context.getString(
            R.string.locale_skin_status,
            resources.configuration.locales.toLanguageTags(),
            context.getString(if (isNight) R.string.locale_skin_dark else R.string.locale_skin_light),
            String.format(Locale.ROOT, "#%08X", context.getColor(R.color.locale_skin_accent)),
            imageValue.string.toString()
        )
        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!rendering) onLanguageSelected(when (checkedId) {
                chineseId -> "zh"
                englishId -> "en"
                else -> ""
            })
        }
        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!rendering) onNightModeSelected(when (checkedId) {
                lightId -> AppCompatDelegate.MODE_NIGHT_NO
                darkId -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            })
        }
    }

    fun render(state: LocaleSkinUiState) {
        // Programmatic selection must not emit another user action.
        rendering = true
        try {
            languageGroup.check(when (state.languageTags) {
                "zh" -> chineseId
                "en" -> englishId
                else -> systemLanguageId
            })
            themeGroup.check(when (state.nightMode) {
                AppCompatDelegate.MODE_NIGHT_NO -> lightId
                AppCompatDelegate.MODE_NIGHT_YES -> darkId
                else -> systemThemeId
            })
        } finally {
            rendering = false
        }
    }

    private fun LinearLayout.addText(textRes: Int, size: Float = 16f): TextView {
        val label = TextView(context).apply {
            setText(textRes)
            textSize = size
            setTextColor(context.getColor(R.color.locale_skin_foreground))
        }
        addView(label, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(12) })
        return label
    }

    private fun LinearLayout.addOptions(vararg options: Pair<Int, Int>): RadioGroup {
        val group = RadioGroup(context).apply {
            orientation = RadioGroup.HORIZONTAL
        }
        options.forEach { (viewId, textRes) ->
            group.addView(RadioButton(context).apply {
                id = viewId
                setText(textRes)
                textSize = 14f
                minHeight = dp(48)
                setTextColor(context.getColor(R.color.locale_skin_foreground))
            }, RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        addView(group, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(16) })
        return group
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

}
