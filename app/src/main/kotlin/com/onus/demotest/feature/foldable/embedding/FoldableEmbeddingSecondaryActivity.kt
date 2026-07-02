package com.onus.demotest.feature.foldable.embedding

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.onus.demotest.feature.foldable.dp

class FoldableEmbeddingSecondaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backButton = Button(this).apply {
            text = "Back"
            isAllCaps = false
        }
        val titleView = TextView(this).apply {
            textSize = 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFF202124.toInt())
            setPadding(0, dp(12), 0, 0)
        }
        val bodyView = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFF3C4043.toInt())
            setLineSpacing(dp(4).toFloat(), 1f)
            setPadding(0, dp(16), 0, 0)
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            setBackgroundColor(0xFFF8FAFD.toInt())
            setPadding(dp(24), dp(24), dp(24), dp(24))
            addView(backButton, LinearLayout.LayoutParams(-2, -2))
            addView(titleView, LinearLayout.LayoutParams(-1, -2))
            addView(bodyView, LinearLayout.LayoutParams(-1, -2))
        })
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        backButton.setOnClickListener {
            finish()
        }
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Embedding detail"
        val body = intent.getStringExtra(EXTRA_BODY)
            ?: "This secondary Activity also works as a normal standalone screen when embedding is not available."
        titleView.text = title
        bodyView.text = body
    }
    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
    }
}
