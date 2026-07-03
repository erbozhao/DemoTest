package com.onus.demotest.feature.foldable

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.slidingpanelayout.widget.SlidingPaneLayout

class ListDetailDemoView2(context: Context) : FrameLayout(context), FoldableBackHandler {
    private val items = sampleMails()
    private var selectedItem = items.first()
    private lateinit var adapter: SimpleTextAdapter
    private lateinit var slidingPaneLayout: SlidingPaneLayout
    private lateinit var detailContainer: FrameLayout
    private var backCallback: OnBackPressedCallback? = null

    init {
        render()
        installBackCallback()
    }

    override fun handleBack(): Boolean {
        if (canCloseDetailPane()) {
            slidingPaneLayout.closePane()
            updateBackCallback()
            return true
        }
        return false
    }

    override fun onDetachedFromWindow() {
        backCallback?.remove()
        backCallback = null
        super.onDetachedFromWindow()
    }

    private fun render() {
        removeAllViews()
        slidingPaneLayout = SlidingPaneLayout(context).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
            lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
            addPanelSlideListener(object : SlidingPaneLayout.PanelSlideListener {
                override fun onPanelSlide(panel: View, slideOffset: Float) = Unit

                override fun onPanelOpened(panel: View) {
                    updateBackCallback()
                }

                override fun onPanelClosed(panel: View) {
                    updateBackCallback()
                }
            })
        }
        detailContainer = FrameLayout(context).apply {
            setBackgroundColor(0xFFF8FAFD.toInt())
        }
        slidingPaneLayout.addView(
            createListPane(),
            SlidingPaneLayout.LayoutParams(context.dp(340), LayoutParams.MATCH_PARENT),
        )
        slidingPaneLayout.addView(
            detailContainer,
            SlidingPaneLayout.LayoutParams(context.dp(300), LayoutParams.MATCH_PARENT).apply {
                weight = 1f
            },
        )
        addView(slidingPaneLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        renderDetailPane()
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
                    this@ListDetailDemoView2.adapter.notifyDataSetChanged()
                    renderDetailPane()
                    slidingPaneLayout.openPane()
                    updateBackCallback()
                }.also { this@ListDetailDemoView2.adapter = it }
            }, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        }
    }

    private fun renderDetailPane() {
        detailContainer.removeAllViews()
        detailContainer.addView(createDetailPane(), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun createDetailPane(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF8FAFD.toInt())
            setPadding(context.dp(22), context.dp(18), context.dp(22), context.dp(18))
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

    private fun installBackCallback() {
        val activity = context as? AppCompatActivity ?: return
        val callback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                slidingPaneLayout.closePane()
                updateBackCallback()
            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, callback)
        backCallback = callback
        updateBackCallback()
    }

    private fun updateBackCallback() {
        backCallback?.isEnabled = canCloseDetailPane()
    }

    private fun canCloseDetailPane(): Boolean {
        return ::slidingPaneLayout.isInitialized &&
            slidingPaneLayout.isSlideable &&
            slidingPaneLayout.isOpen
    }
}
