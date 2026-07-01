package com.onus.demotest.pages.ui.tab

import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author: onuszhao
 * @Date: 2024-07-04 15:17
 * @Description:
 */
class ViewPagerAdapter : RecyclerView.Adapter< RecyclerView.ViewHolder >() {
    private var items = mutableListOf<String>()

    private var dates = mutableListOf<String>().apply {
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
        add("Test  TestTest  Test  Test")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder  {
        val itemView = RecyclerView(parent.context).apply {
            overScrollMode = OVER_SCROLL_NEVER
            layoutManager = LinearLayoutManager(context)
            adapter = RecyclerViewAdapter()
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        return object : RecyclerView.ViewHolder(itemView) {}
    }

    override fun onBindViewHolder(holder:  RecyclerView.ViewHolder , position: Int) {
        (holder.itemView as? RecyclerView)?.let {
            it.setBackgroundColor(if (position == 0) android.graphics.Color.RED else if (position == 1) android.graphics.Color.BLUE else android.graphics.Color.GRAY)
            (it.adapter as? RecyclerViewAdapter)?.setData(dates)
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