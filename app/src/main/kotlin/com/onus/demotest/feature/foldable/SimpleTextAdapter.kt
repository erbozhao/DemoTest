package com.onus.demotest.feature.foldable

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SimpleTextAdapter(
    private var items: List<FoldableItem>,
    private val selectedIdProvider: () -> Int? = { null },
    private val onClick: (FoldableItem) -> Unit,
) : RecyclerView.Adapter<SimpleTextAdapter.Holder>() {

    fun submitItems(next: List<FoldableItem>) {
        items = next
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val container = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        val title = TextView(parent.context).apply {
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF202124.toInt())
        }
        val subtitle = TextView(parent.context).apply {
            textSize = 13f
            setTextColor(0xFF5F6368.toInt())
        }
        container.addView(title)
        container.addView(subtitle)
        return Holder(container, title, subtitle)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
        holder.itemView.setBackgroundColor(
            if (selectedIdProvider() == item.id) 0xFFE8F0FE.toInt() else 0xFFFFFFFF.toInt()
        )
        holder.itemView.setOnClickListener { onClick(item) }
    }

    class Holder(
        view: LinearLayout,
        val title: TextView,
        val subtitle: TextView,
    ) : RecyclerView.ViewHolder(view)
}

internal fun android.view.View.dp(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()

internal fun android.content.Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()
