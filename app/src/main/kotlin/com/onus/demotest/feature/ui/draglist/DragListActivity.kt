package com.onus.demotest.feature.ui.draglist

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.View.DragShadowBuilder
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.common.DeviceUtils
import java.util.Collections

/**
 * @Author: onuszhao
 * @Date: 2025-01-23 15:28
 * @Description:
 */
class DragListActivity : AppCompatActivity(), ItemListener, View.OnDragListener {

    private lateinit var container: LinearLayout
    private lateinit var listView: RecyclerView
    private lateinit var listAdapter: DragListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@DragListActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)

        listView = RecyclerView(this).apply {
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            layoutManager = LinearLayoutManager(context)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        container.addView(listView)

        listAdapter = DragListAdapter().apply {
            listView.adapter = this
        }

        initData()
    }

    private fun initData() {
        val date = mutableListOf<String>().apply {
            repeat(30) { index ->
                add("This list item is $index")
            }
        }
        listAdapter.setData(date)

        listAdapter.setItemListener(this@DragListActivity)
        listView.setOnDragListener(this@DragListActivity)
    }

    /** 可以通过ItemTouchHelper.Callback实现 */
    private var draggedIndex = -1
    override fun onLongClick(view: View, position: Int) {
        // 开始拖拽
        draggedIndex = position
        val data = ClipData.newPlainText("", "")
        val shadowBuilder = DragShadowBuilder(view)
        view.startDragAndDrop(data, shadowBuilder, view, 0)
    }

    override fun onDrag(v: View?, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Toast.makeText(this@DragListActivity, "Drag started", Toast.LENGTH_SHORT).show()
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                // 获取目标位置索引
                var targetIndex: Int = ListView.INVALID_POSITION
                val childViewUnder = listView.findChildViewUnder(event.x, event.y)
                if (childViewUnder != null) {
                    val childViewHolder = listView.getChildViewHolder(childViewUnder)
                    if (childViewHolder != null) {
                        targetIndex = childViewHolder.getAdapterPosition()
                    }
                }
                // val targetIndex: Int = listView.pointToPosition(event!!.x.toInt(), event!!.y.toInt())
                if (targetIndex != ListView.INVALID_POSITION && targetIndex != draggedIndex) {
                    // 交换数据
                    Collections.swap(listAdapter.items, draggedIndex, targetIndex)
                    listAdapter.notifyDataSetChanged()
                    draggedIndex = targetIndex
                }
            }

            DragEvent.ACTION_DROP -> {
                Toast.makeText(this@DragListActivity, "Drag dropped", Toast.LENGTH_SHORT).show()
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                draggedIndex = -1
            }
        }
        return true
    }
}