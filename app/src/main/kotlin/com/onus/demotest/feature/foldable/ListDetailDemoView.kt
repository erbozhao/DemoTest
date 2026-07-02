package com.onus.demotest.feature.foldable

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListDetailDemoView(context: Context) : FrameLayout(context), FoldableBackHandler {
    private val items = sampleMails()
    private var selectedItem = items.first()
    private var showingDetailCompact = false
    private var lastExpanded = false
    private lateinit var adapter: SimpleTextAdapter

    init {
        lastExpanded = isExpanded()
        render()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldw != 0 && w != oldw) {
            val expanded = isExpanded()
            if (expanded != lastExpanded) {
                showingDetailCompact = false
                lastExpanded = expanded
            }
            render()
        }
    }

    override fun handleBack(): Boolean {
        if (!isExpanded() && showingDetailCompact) {
            showingDetailCompact = false
            render()
            return true
        }
        return false
    }

    private fun render() {
        removeAllViews()
        val expanded = isExpanded()
        lastExpanded = expanded
        addView(
            LinearLayout(context).apply {
                orientation = if (expanded) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                setBackgroundColor(0xFFFFFFFF.toInt())
                if (expanded) {
                    addView(createListPane(), LinearLayout.LayoutParams(context.dp(340), -1))
                    addView(createDetailPane(showBack = false), LinearLayout.LayoutParams(0, -1, 1f))
                } else if (showingDetailCompact) {
                    addView(createDetailPane(showBack = true), LinearLayout.LayoutParams(-1, -1))
                } else {
                    addView(createListPane(), LinearLayout.LayoutParams(-1, -1))
                }
            },
            LayoutParams(-1, -1),
        )
    }

    private fun createListPane(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            addView(TextView(context).apply {
                text = "Inbox"
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF202124.toInt())
                setPadding(context.dp(16), context.dp(18), context.dp(16), context.dp(14))
            })
            addView(RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = SimpleTextAdapter(items, { selectedItem.id }) { item ->
                    selectedItem = item
                    showingDetailCompact = true
                    this@ListDetailDemoView.adapter.notifyDataSetChanged()
                    render()
                }.also { this@ListDetailDemoView.adapter = it }
            }, LinearLayout.LayoutParams(-1, 0, 1f))
        }
    }

    private fun createDetailPane(showBack: Boolean): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF8FAFD.toInt())
            setPadding(context.dp(22), context.dp(18), context.dp(22), context.dp(18))
            if (showBack) {
                addView(Button(context).apply {
                    text = "Back"
                    isAllCaps = false
                    setOnClickListener { handleBack() }
                })
            }
            addView(TextView(context).apply {
                text = selectedItem.title
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF202124.toInt())
                setPadding(0, context.dp(12), 0, 0)
            })
            addView(TextView(context).apply {
                text = selectedItem.subtitle
                textSize = 14f
                setTextColor(0xFF5F6368.toInt())
                setPadding(0, context.dp(6), 0, 0)
            })
            addView(TextView(context).apply {
                text = selectedItem.body
                textSize = 16f
                setTextColor(0xFF3C4043.toInt())
                setLineSpacing(context.dp(4).toFloat(), 1f)
                setPadding(0, context.dp(18), 0, 0)
            })
        }
    }

    private fun isExpanded(): Boolean {
        val width = if (width > 0) width else resources.displayMetrics.widthPixels
        val widthDp = width / resources.displayMetrics.density
        return widthDp >= 600f
    }
}
