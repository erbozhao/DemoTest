package com.onus.demotest.feature.foldable

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.onus.demotest.feature.foldable.embedding.FoldableEmbeddingPrimaryActivity

class FoldableDemoActivity : AppCompatActivity() {

    private lateinit var tabContainer: LinearLayout
    private lateinit var contentContainer: FrameLayout
    private var selectedType = FoldableDemoType.LIST_DETAIL
    private var currentBackHandler: FoldableBackHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createRootView())
        selectedType = savedInstanceState?.getString(KEY_SELECTED_TYPE)
            ?.let { runCatching { FoldableDemoType.valueOf(it) }.getOrNull() }
            ?: FoldableDemoType.LIST_DETAIL

        renderTabs()
        renderDemo(selectedType)
    }

    private fun createRootView(): LinearLayout {
        tabContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        contentContainer = FrameLayout(this)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            fitsSystemWindows = true
            addView(HorizontalScrollView(this@FoldableDemoActivity).apply {
                isHorizontalScrollBarEnabled = false
                isFillViewport = false
                addView(tabContainer, FrameLayout.LayoutParams(-2, -2))
            }, LinearLayout.LayoutParams(-1, -2))
            addView(contentContainer, LinearLayout.LayoutParams(-1, 0, 1f))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_SELECTED_TYPE, selectedType.name)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (currentBackHandler?.handleBack() == true) {
            return
        }
        super.onBackPressed()
    }

    private fun renderTabs() {
        tabContainer.removeAllViews()
        FoldableDemoType.entries.forEach { type ->
            val button = TextView(this).apply {
                gravity = android.view.Gravity.CENTER
                text = type.title
                textSize = 12f
                minWidth = dp(if (type == FoldableDemoType.ACTIVITY_EMBEDDING) 150 else 112)
                minHeight = dp(40)
                setPadding(dp(14), 0, dp(14), 0)
                val selected = type == selectedType
                setTextColor(if (selected) Color.WHITE else Color.rgb(60, 64, 67))
                typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                background = tabBackground(selected)
                setOnClickListener {
                    if (type == FoldableDemoType.ACTIVITY_EMBEDDING) {
                        startActivity(Intent(this@FoldableDemoActivity, FoldableEmbeddingPrimaryActivity::class.java))
                        return@setOnClickListener
                    }
                    selectedType = type
                    renderTabs()
                    renderDemo(type)
                }
                isSelected = selected
            }
            tabContainer.addView(button, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(40)).apply {
                setMargins(dp(4), dp(2), dp(4), dp(2))
            })
        }
        tabContainer.post {
            val selectedTab = (0 until tabContainer.childCount)
                .map { tabContainer.getChildAt(it) }
                .firstOrNull { it.isSelected }
            val parent = tabContainer.parent as? HorizontalScrollView
            if (selectedTab != null && parent != null) {
                parent.smoothScrollTo(selectedTab.left - dp(12), 0)
            }
        }
    }

    private fun renderDemo(type: FoldableDemoType) {
        contentContainer.removeAllViews()
        val view = when (type) {
            FoldableDemoType.LIST_DETAIL -> ListDetailDemoView(this)
            FoldableDemoType.LIST_DETAIL_SLIDING -> ListDetailDemoView2(this)
            FoldableDemoType.FEED -> FeedDemoView(this)
            FoldableDemoType.FILES -> FilesDemoView(this)
            FoldableDemoType.SUPPORTING_PANE -> SupportingPaneDemoView(this)
            FoldableDemoType.ADAPTIVE_NAVIGATION -> AdaptiveNavigationDemoView(this)
            FoldableDemoType.ACTIVITY_EMBEDDING -> error("Handled above")
        }
        currentBackHandler = view as? FoldableBackHandler
        contentContainer.addView(view)
    }

    private fun tabBackground(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dp(6).toFloat()
            setColor(if (selected) Color.rgb(25, 103, 210) else Color.rgb(232, 234, 237))
            if (selected) {
                setStroke(dp(2), Color.rgb(15, 82, 186))
            }
        }
    }

    companion object {
        private const val KEY_SELECTED_TYPE = "selected_type"
    }
}
