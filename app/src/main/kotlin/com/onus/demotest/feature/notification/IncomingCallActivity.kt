package com.onus.demotest.feature.notification

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.common.DeviceUtils

/**
 * @author onuszhao
 * @since 2025/12/3
 * @description
 */

class IncomingCallActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var scrollView: ScrollView
    private lateinit var acceptBtn: Button
    private lateinit var rejectBtn: Button

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        scrollView = ScrollView(this).apply {
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@IncomingCallActivity), 0, 0)
        }
        setContentView(scrollView)
        ActivityStateManager.setCurActivity(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTheme(R.style.ActivityMain_Light)
        } else {
            setTheme(R.style.ActivityMain)
        }

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            scrollView.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        }

        acceptBtn = Button(this).apply {
            isAllCaps = false
            text = "接听"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@IncomingCallActivity)
        }
        container.addView(acceptBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        rejectBtn = Button(this).apply {
            isAllCaps = false
            text = "拒绝"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@IncomingCallActivity)
        }
        container.addView(rejectBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v) {
            acceptBtn -> {
            }

            rejectBtn -> {
            }
        }
    }
}

