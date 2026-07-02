package com.onus.demotest.feature.foldable

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class FilesDemoView(context: Context) : FrameLayout(context) {
    private val categories = listOf("Storage\n580", "Videos\n10", "Music\n32", "Documents\n36", "Image\n900", "Archives\n12", "WhatsApp\n10", "More\n10")

    init {
        render()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldw != 0 && w != oldw) {
            render()
        }
    }

    private fun render() {
        removeAllViews()
        val expanded = widthDp() >= 600
        val root = LinearLayout(context).apply {
            orientation = if (expanded) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            setBackgroundColor(0xFFF8FAFC.toInt())
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        }
        val main = scrollColumn {
            addView(title("Files"))
            addSpaced(quickActions())
            addSpaced(categoryGrid())
            addSpaced(recentFiles())
            if (!expanded) addSpaced(cleanCard())
        }
        root.addView(main, LinearLayout.LayoutParams(0, -1, if (expanded) 1f else 0f).apply {
            if (!expanded) {
                width = -1
                height = -1
                weight = 0f
            }
        })
        if (expanded) {
            root.addView(scrollColumn {
                setPadding(context.dp(16), context.dp(56), 0, 0)
                addSpaced(cleanCard())
            }, LinearLayout.LayoutParams(0, -1, 1f))
        }
        addView(root, LayoutParams(-1, -1))
    }

    private fun quickActions(): View = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(card("Downloads", "Easy & Fast", 0xFFEDE7F6.toInt()), LinearLayout.LayoutParams(0, context.dp(74), 1f).apply { setMargins(0, 0, context.dp(8), context.dp(12)) })
        addView(card("PDF Tools", "PDF", 0xFFFFEBEE.toInt()), LinearLayout.LayoutParams(0, context.dp(74), 1f).apply { setMargins(context.dp(8), 0, 0, context.dp(12)) })
    }

    private fun categoryGrid(): View = panel("Categories").apply {
        val grid = GridLayout(context).apply {
            columnCount = 4
            categories.forEach {
                addView(card(it.substringBefore('\n'), it.substringAfter('\n'), 0xFFFFFFFF.toInt()), GridLayout.LayoutParams().apply {
                    width = 0
                    height = context.dp(92)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(context.dp(4), context.dp(4), context.dp(4), context.dp(4))
                })
            }
        }
        addView(grid)
    }

    private fun recentFiles(): View = panel("Recent files").apply {
        addView(card("PDF  CabinaArmadio_doppia.pdf", "WhatsApp opened May 23, 2021", 0xFFFFFFFF.toInt()))
        addView(card("EXL  Internet Infrastructure 2020", "WhatsApp opened May 23, 2021", 0xFFFFFFFF.toInt()))
    }

    private fun cleanCard(): View = panel("Clean up").apply {
        addView(card("Videos", "Clean 23.5 GB", 0xFFFFFFFF.toInt()))
        addView(card("WhatsApp", "Clean 420.5 MB", 0xFFFFFFFF.toInt()))
        addView(card("Browsing data", "Clean 86.3 MB", 0xFFFFFFFF.toInt()))
        addView(card("Large files", "Clean 5.2 GB", 0xFFFFFFFF.toInt()))
    }

    private fun scrollColumn(block: LinearLayout.() -> Unit): ScrollView {
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

    private fun panel(title: String): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(12))
        setBackgroundColor(0xFFFFFFFF.toInt())
        addView(TextView(context).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF111827.toInt())
        })
    }

    private fun card(title: String, subtitle: String, color: Int): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(context.dp(14), context.dp(10), context.dp(14), context.dp(10))
        setBackgroundColor(color)
        addView(TextView(context).apply {
            text = title
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF202124.toInt())
        })
        addView(TextView(context).apply {
            text = subtitle
            textSize = 13f
            setTextColor(0xFF5F6368.toInt())
        })
    }

    private fun title(textValue: String): View = TextView(context).apply {
        text = textValue
        textSize = 24f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(0xFF111827.toInt())
        setPadding(0, 0, 0, context.dp(14))
    }

    private fun widthDp(): Int {
        val w = if (width > 0) width else resources.displayMetrics.widthPixels
        return (w / resources.displayMetrics.density).toInt()
    }
}
