package com.onus.demotest.feature.ui.draglist

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onus.demotest.R

/**
 * @Author: onuszhao
 * @Date: 2025-01-23 15:31
 * @Description:
 */
class DragListAdapter: RecyclerView.Adapter< RecyclerView.ViewHolder >() {

    var items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder  {
        val itemView = TextView(parent.context).apply {
            setBackgroundColor(Color.BLUE)
            textSize = resources.getDimension(R.dimen.dp_6)
            setTextColor(resources.getColor(R.color.black))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelOffset(R.dimen.dp_40))
        }
        return object : RecyclerView.ViewHolder(itemView) {}
    }

    override fun onBindViewHolder(holder:  RecyclerView.ViewHolder , position: Int) {
        items.getOrNull(position)?.let {
            (holder.itemView as? TextView)?.let { itemView ->
                itemView.text = it
                itemView.setOnLongClickListener {
                    itemListener?.onLongClick(it, position)
                    return@setOnLongClickListener true
                }
            }
        }

    }

    private var itemListener: ItemListener? = null
    fun setItemListener(listener: ItemListener) {
        itemListener = listener
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

interface ItemListener {
    fun onLongClick(view: View, position: Int)
}