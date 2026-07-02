package com.onus.demotest.feature.foldable.embedding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onus.demotest.feature.foldable.FoldableItem
import com.onus.demotest.feature.foldable.SimpleTextAdapter
import com.onus.demotest.feature.foldable.dp

class FoldableEmbeddingPrimaryActivity : AppCompatActivity() {

    private val items = listOf(
        FoldableItem(1, "Split overview", "Primary pane", "Activity Embedding keeps a primary list and secondary detail activity together when the window supports it."),
        FoldableItem(2, "Supported device", "Side-by-side", "On large screens, foldables, or compatible multi-window modes, the secondary activity can appear beside this one."),
        FoldableItem(3, "Fallback", "Single activity", "If embedding is unavailable, the secondary activity opens normally and still shows the selected detail."),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backButton = Button(this).apply {
            text = "Back to demos"
            isAllCaps = false
        }
        val titleView = TextView(this).apply {
            text = "Activity Embedding"
            textSize = 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFF202124.toInt())
            setPadding(dp(20), dp(8), dp(20), dp(12))
        }
        val descriptionView = TextView(this).apply {
            text = "Tap an item. Supported large-screen environments can keep the secondary Activity beside this primary pane."
            textSize = 14f
            setTextColor(0xFF5F6368.toInt())
            setPadding(dp(20), 0, dp(20), dp(12))
        }
        val recyclerView = RecyclerView(this)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            setBackgroundColor(0xFFFFFFFF.toInt())
            addView(backButton, LinearLayout.LayoutParams(-2, -2).apply {
                setMargins(dp(12), dp(12), 0, 0)
            })
            addView(titleView, LinearLayout.LayoutParams(-1, -2))
            addView(descriptionView, LinearLayout.LayoutParams(-1, -2))
            addView(recyclerView, LinearLayout.LayoutParams(-1, 0, 1f))
        })
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        backButton.setOnClickListener {
            finish()
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FoldableEmbeddingPrimaryActivity)
            adapter = SimpleTextAdapter(items) { item ->
                startActivity(
                    Intent(this@FoldableEmbeddingPrimaryActivity, FoldableEmbeddingSecondaryActivity::class.java)
                        .putExtra(FoldableEmbeddingSecondaryActivity.EXTRA_ID, item.id)
                        .putExtra(FoldableEmbeddingSecondaryActivity.EXTRA_TITLE, item.title)
                        .putExtra(FoldableEmbeddingSecondaryActivity.EXTRA_BODY, item.body)
                )
            }
        }
    }
}
