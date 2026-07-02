package com.onus.demotest.feature.foldable

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class AdaptiveNavigationDemoView(context: Context) : FrameLayout(context) {
    private enum class Destination(val label: String, val title: String, val body: String) {
        HOME("Home", "Adaptive home", "Navigation changes placement as the available width grows."),
        MESSAGES("Messages", "Inbox focus", "Content state stays stable while navigation chrome adapts."),
        TASKS("Tasks", "Work queue", "A single destination model serves phones, tablets, and foldables."),
        SETTINGS("Settings", "Preferences", "Large screens can expose more navigation context."),
    }

    private var selectedDestination = Destination.HOME

    init {
        setBackgroundColor(0xFFFFFFFF.toInt())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { render() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            render()
        }
    }

    private fun render() {
        removeAllViews()
        val widthDp = (width / resources.displayMetrics.density).toInt()
        val mode = when {
            widthDp >= 840 -> NavMode.WIDE
            widthDp >= 600 -> NavMode.RAIL
            else -> NavMode.BOTTOM
        }
        addView(createLayoutForMode(mode), LayoutParams(-1, -1))
    }

    private fun createLayoutForMode(mode: NavMode): LinearLayout {
        val container = LinearLayout(context).apply {
            orientation = if (mode == NavMode.BOTTOM) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        if (mode == NavMode.BOTTOM) {
            container.addView(contentView(), LinearLayout.LayoutParams(-1, 0, 1f))
            container.addView(navBar(horizontal = true), LinearLayout.LayoutParams(-1, context.dp(64)))
        } else {
            val navWidth = if (mode == NavMode.WIDE) context.dp(220) else context.dp(84)
            container.addView(navBar(horizontal = false), LinearLayout.LayoutParams(navWidth, -1))
            container.addView(contentView(), LinearLayout.LayoutParams(0, -1, 1f))
        }
        return container
    }

    private fun navBar(horizontal: Boolean): LinearLayout {
        return LinearLayout(context).apply {
            orientation = if (horizontal) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFFE8F0FE.toInt())
            Destination.entries.forEach { destination ->
                addView(Button(context).apply {
                    text = destination.label
                    isAllCaps = false
                    textSize = 12f
                    setOnClickListener {
                        selectedDestination = destination
                        render()
                    }
                }, LinearLayout.LayoutParams(if (horizontal) 0 else -1, if (horizontal) -1 else context.dp(64), if (horizontal) 1f else 0f))
            }
        }
    }

    private fun contentView(): LinearLayout {
        val destination = selectedDestination
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(24), context.dp(24), context.dp(24), context.dp(24))
            addView(TextView(context).apply {
                text = destination.title
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF202124.toInt())
            })
            addView(TextView(context).apply {
                text = destination.body
                textSize = 16f
                setTextColor(0xFF3C4043.toInt())
                setPadding(0, context.dp(12), 0, 0)
            }, ViewGroup.LayoutParams(-1, -2))
            addView(TextView(context).apply {
                text = "Resize the window: compact uses bottom navigation, medium uses a rail, expanded uses a wider navigation surface."
                textSize = 14f
                setTextColor(0xFF5F6368.toInt())
                setPadding(0, context.dp(18), 0, 0)
            }, ViewGroup.LayoutParams(-1, -2))
        }
    }

    private enum class NavMode {
        BOTTOM,
        RAIL,
        WIDE,
    }
}
