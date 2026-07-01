package com.onus.demotest.feature.ui.tab

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager

/**
 * @Author: onuszhao
 * @Date: 2024-07-04 10:55
 * @Description:
 */
class TabActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var container: LinearLayout
    private lateinit var tableLayout: TableLayout
    private lateinit var tab1: TextView
    private lateinit var tab2: TextView
    private lateinit var tab3: TextView
    private lateinit var viewPagerAdapter : ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)

        val tableLayout = TableLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, resources.getDimensionPixelOffset(R.dimen.dp_52))
        }
        container.addView(tableLayout)

        val tableRow = TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT)
        }.also {
            it.addView(TextView(this).apply {
                tab1 = this
                id = ID_TAB_1
                gravity = Gravity.CENTER
                text = "tab1"
                textSize = resources.getDimension(R.dimen.dp_6)
                setTextColor(resources.getColor(R.color.black))
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT).apply { weight = 1f }
            })
            it.addView(TextView(this).apply {
                tab2 = this
                id = ID_TAB_2
                gravity = Gravity.CENTER
                text = "tab2"
                textSize = resources.getDimension(R.dimen.dp_6)
                setTextColor(resources.getColor(R.color.black))
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT).apply { weight = 1f }
            })
            it.addView(TextView(this).apply {
                tab3 = this
                id = ID_TAB_3
                gravity = Gravity.CENTER
                text = "tab3"
                textSize = resources.getDimension(R.dimen.dp_6)
                setTextColor(resources.getColor(R.color.black))
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT).apply { weight = 1f }
            })
        }
        tableLayout.addView(tableRow)

        val viewPager = ViewPager2(this).apply{
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
        container.addView(viewPager, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))


        viewPagerAdapter = ViewPagerAdapter().apply {
            viewPager.adapter = this
        }

        // val recyclerView = RecyclerView(this).apply {
        //     layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        // }
        // container.addView(recyclerView)

        initAction()
    }

    private fun initAction() {
        tab1.setOnClickListener(this)
        tab2.setOnClickListener(this)
        tab3.setOnClickListener(this)
        viewPagerAdapter.setData(mutableListOf<String>().apply {
            add("1")
            add("2")
            add("3")
        })
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v.id) {
            ID_TAB_1 -> {
            }
            ID_TAB_2 -> {
            }
            ID_TAB_3 -> {
            }
        }
    }

    companion object {
        val ID_TAB_1 = View.generateViewId()
        val ID_TAB_2 = View.generateViewId()
        val ID_TAB_3 = View.generateViewId()
    }
}