package com.onus.demotest.feature.foldable

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class SupportingPaneDemoView(context: Context) : FrameLayout(context), FoldableBackHandler {
    private var showingSupportCompact = false
    private val sections = listOf(
        FoldableItem(1, "Adaptive reading surface", "Main pane", "A supporting pane keeps related material close to the document without turning the page into a dense dashboard."),
        FoldableItem(2, "Compact navigation", "Main pane", "When the window only has room for one pane, the main document remains the primary destination."),
        FoldableItem(3, "Expanded posture", "Main pane", "On a larger unfolded screen, notes can stay visible on the side."),
    )
    private var selected = sections.first()

    init {
        render()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldw != 0 && w != oldw) render()
    }

    override fun handleBack(): Boolean {
        if (!isExpanded() && showingSupportCompact) {
            showingSupportCompact = false
            render()
            return true
        }
        return false
    }

    private fun render() {
        removeAllViews()
        val expanded = isExpanded()
        val root = LinearLayout(context).apply {
            orientation = if (expanded) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            setBackgroundColor(0xFFF8FAFC.toInt())
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        }
        if (expanded) {
            root.addView(documentPane(false), LinearLayout.LayoutParams(0, -1, 2f))
            root.addView(supportPane(false), LinearLayout.LayoutParams(0, -1, 1f).apply { setMargins(context.dp(16), 0, 0, 0) })
            if (widthDp() >= 1000) {
                root.addView(extraPane(), LinearLayout.LayoutParams(0, -1, 1f).apply { setMargins(context.dp(16), 0, 0, 0) })
            }
        } else {
            root.addView(if (showingSupportCompact) supportPane(true) else documentPane(true), LinearLayout.LayoutParams(-1, -1))
        }
        addView(root, LayoutParams(-1, -1))
    }

    private fun documentPane(showOpenButton: Boolean): View = scrollPanel {
        addView(title("Foldable Reading"))
        addSpaced(card(selected.title, selected.body))
        if (showOpenButton) {
            addSpaced(Button(context).apply {
                text = "Open notes"
                isAllCaps = false
                setOnClickListener {
                    showingSupportCompact = true
                    render()
                }
            })
        }
        sections.forEach { section ->
            addSpaced(card(section.title, section.body).apply {
                setOnClickListener {
                    selected = section
                    render()
                }
            })
        }
    }

    private fun supportPane(showBack: Boolean): View = scrollPanel {
        if (showBack) {
            addView(Button(context).apply {
                text = "Back"
                isAllCaps = false
                setOnClickListener { handleBack() }
            })
        }
        addView(title("Notes"))
        addSpaced(card("Context note", "Use the supporting pane for contextual notes, outline entries, references, and actions tied to the current document."))
        addSpaced(title("Outline"))
        sections.forEach { section ->
            addSpaced(card(section.title, section.subtitle).apply {
                setOnClickListener {
                    selected = section
                    showingSupportCompact = false
                    render()
                }
            })
        }
    }

    private fun extraPane(): View = scrollPanel {
        addView(title("Tools"))
        listOf("Canonical layouts", "Window size classes", "Foldable posture", "Theme: system", "Sync notes: enabled").forEach {
            addSpaced(card(it, "Reference"))
        }
    }

    private fun scrollPanel(block: LinearLayout.() -> Unit): ScrollView {
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            block()
        }
        return ScrollView(context).apply {
            isFillViewport = true
            addView(column, LayoutParams(-1, -2))
        }
    }

    private fun LinearLayout.addSpaced(view: View) {
        addView(view, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, context.dp(12), 0, 0)
        })
    }

    private fun title(textValue: String): View = TextView(context).apply {
        text = textValue
        textSize = 22f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(0xFF111827.toInt())
        setPadding(0, 0, 0, context.dp(12))
    }

    private fun card(title: String, body: String): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(context.dp(16), context.dp(14), context.dp(16), context.dp(14))
        setBackgroundColor(0xFFFFFFFF.toInt())
        addView(TextView(context).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF202124.toInt())
        })
        addView(TextView(context).apply {
            text = body
            textSize = 14f
            setTextColor(0xFF5F6368.toInt())
        })
    }

    private fun isExpanded(): Boolean = widthDp() >= 600

    private fun widthDp(): Int {
        val w = if (width > 0) width else resources.displayMetrics.widthPixels
        return (w / resources.displayMetrics.density).toInt()
    }
}
