package com.onus.demotest.feature.foldable

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FeedDemoView(context: Context) : FrameLayout(context) {
    private val items = listOf(
        FoldableItem(1, "Foldable feeds need hierarchy", "Featured - 6 min", "A feed layout uses the available width to move from one compact column into a richer multi-column surface."),
        FoldableItem(2, "Compact phones", "Compact - 3 min", "Cards remain in a single vertical stream for comfortable scanning."),
        FoldableItem(3, "Medium width", "Tablet - 4 min", "Two columns keep more stories visible without changing the reading model."),
        FoldableItem(4, "Expanded foldables", "Foldable - 5 min", "Three or more columns make unfolded screens feel intentionally designed."),
        FoldableItem(5, "Window size classes", "Android - 4 min", "Width classes provide a stable way to choose the right feed density."),
        FoldableItem(6, "Visual rhythm", "Design - 2 min", "Featured cards span wider tracks while regular cards keep the grid predictable."),
    )
    private val gridLayoutManager = GridLayoutManager(context, 1)
    private val feedAdapter = FeedAdapter(items) { position, columns ->
        when {
            columns <= 1 -> 1
            position == 0 -> columns
            position == 3 -> minOf(2, columns)
            else -> 1
        }
    }

    init {
        val recyclerView = RecyclerView(context).apply {
            layoutManager = gridLayoutManager
            adapter = feedAdapter
            clipToPadding = false
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply {
                text = "Adaptive feed"
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF202124.toInt())
                setPadding(dp(16), dp(16), dp(16), dp(16))
            }, LinearLayout.LayoutParams(-1, -2))
            addView(recyclerView, LinearLayout.LayoutParams(-1, 0, 1f))
        }, LayoutParams(-1, -1))
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = feedAdapter.spanSize(position, gridLayoutManager.spanCount)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val widthDp = (w / resources.displayMetrics.density).toInt()
        gridLayoutManager.spanCount = when {
            widthDp >= 1200 -> 4
            widthDp >= 840 -> 3
            widthDp >= 600 -> 2
            else -> 1
        }
        gridLayoutManager.spanSizeLookup.invalidateSpanIndexCache()
        feedAdapter.notifyDataSetChanged()
    }

    private class FeedAdapter(
        private val items: List<FoldableItem>,
        private val spanResolver: (Int, Int) -> Int,
    ) : RecyclerView.Adapter<FeedAdapter.Holder>() {
        fun spanSize(position: Int, columns: Int): Int = spanResolver(position, columns)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val container = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                setBackgroundColor(0xFFF1F3F4.toInt())
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    setMargins(dp(8), dp(8), dp(8), dp(8))
                }
            }
            val title = TextView(parent.context).apply {
                textSize = 17f
                setTextColor(0xFF202124.toInt())
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val category = TextView(parent.context).apply {
                textSize = 12f
                setTextColor(0xFF1967D2.toInt())
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val body = TextView(parent.context).apply {
                textSize = 14f
                setTextColor(0xFF5F6368.toInt())
            }
            container.addView(category)
            container.addView(title)
            container.addView(body)
            return Holder(container, title, body, category)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.body.text = item.body
            holder.category.text = item.subtitle
            val featured = position == 0 || position == 3
            holder.itemView.setBackgroundColor(if (featured) 0xFFDDF4EF.toInt() else 0xFFF1F3F4.toInt())
        }

        class Holder(view: LinearLayout, val title: TextView, val body: TextView, val category: TextView) :
            RecyclerView.ViewHolder(view)
    }
}
