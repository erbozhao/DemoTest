package com.onus.demotest.feature.ad

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.onus.demotest.R
import com.onus.demotest.common.DeviceUtils

/** Marketing entry point. Ad/Play presentation logic stays out of the home page. */
class AdDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            setPaddingRelative(24, DeviceUtils.getStatusBarHeight(this@AdDemoActivity), 24, 24)
        }
        setContentView(container)

        val title = TextView(this).apply {
            text = "Ad Demo"
            setTextColor(getColor(R.color.black))
            textSize = 20f
        }
        container.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val description = TextView(this).apply {
            text = "Open a Google Play Inline Install half sheet from this page."
            setTextColor(getColor(R.color.black))
            textSize = 14f
        }
        container.addView(description, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 12 })

        val inlineInstallButton = Button(this).apply {
            isAllCaps = false
            text = "inlineInstall"
            setOnClickListener {
            launchInlineInstall()
            }
        }
        container.addView(inlineInstallButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 20 })
    }

    /** Opens the Play listing as an inline-install half sheet when supported. */
    private fun launchInlineInstall() {
        // Keep this flow aligned with Google's official Inline Install example.
        val intent = Intent(Intent.ACTION_VIEW)
        val referrer = "channel%3Dgoogle_inline%26campaign%3Dtest_20260929%26material%3Ddemo_a%26book_id%3D12345"
        val id = "com.cloudview.novel"
        val callerId = packageName
        val cslId = "csl_test_01"
        intent.setPackage("com.android.vending")
        val deepLinkUrl = "https://play.google.com/d?id=$id&referrer=$referrer&listing=$cslId"
        intent.data = Uri.parse(deepLinkUrl)
        intent.putExtra("overlay", true)
        intent.putExtra("callerId", callerId)
        val packageManager = packageManager
        if (intent.resolveActivity(packageManager) != null) {
            try {
                startActivityForResult(intent, 0)
            } catch (_: ActivityNotFoundException) {
                openFullPlayStoreListing(id)
            }
        } else {
            // Fallback to deep linking to the full Play Store listing.
            openFullPlayStoreListing(id)
        }
    }

    private fun openFullPlayStoreListing(targetPackage: String) {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$targetPackage"))
        try {
            startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$targetPackage")))
        }
    }
}
