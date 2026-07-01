package com.onus.demotest.feature.ui.tab

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onus.demotest.R

/**
 * @Author: onuszhao
 * @Date: 2024-07-04 15:17
 * @Description:
 */
class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = TextView(parent.context).apply {
            gravity = Gravity.CENTER
            text = "tab2"
            textSize = resources.getDimension(R.dimen.dp_6)
            setTextColor(resources.getColor(R.color.black))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,  resources.getDimensionPixelOffset(R.dimen.dp_52))
        }
        return object : RecyclerView.ViewHolder(itemView) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items.getOrNull(position)?.let { data ->
            (holder.itemView as? TextView)?.text = data
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setData(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}