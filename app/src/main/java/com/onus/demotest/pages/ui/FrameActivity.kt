package com.onus.demotest.pages.ui

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.utils.DeviceUtils

/**
 * @Author: onuszhao
 * @Date: 2024-10-18 14:27
 * @Description:
 */
class FrameActivity: AppCompatActivity() {


    private lateinit var container: FrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        container = FrameLayout(this).apply {
            id = System.currentTimeMillis().toInt()
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@FrameActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTheme(R.style.ActivityMain_Light)
        } else {
            setTheme(R.style.ActivityMain)
        }

        val test1 = FrameLayout(this).apply {
            setBackgroundResource(R.color.purple_200)
        }
        container.addView(test1, FrameLayout.LayoutParams(resources.getDimensionPixelOffset(R.dimen.dp_50), resources.getDimensionPixelOffset(R.dimen.dp_50)).apply {
            gravity = Gravity.CENTER
            marginEnd = resources.getDimensionPixelOffset(R.dimen.dp_50)
            bottomMargin = resources.getDimensionPixelOffset(R.dimen.dp_50)
        })


        val test2 = FrameLayout(this).apply {
            setBackgroundResource(R.color.black)
        }
        container.addView(test2, FrameLayout.LayoutParams(resources.getDimensionPixelOffset(R.dimen.dp_50), resources.getDimensionPixelOffset(R.dimen.dp_50)).apply {
            gravity = Gravity.CENTER
            marginEnd = resources.getDimensionPixelOffset(R.dimen.dp_30)
            bottomMargin = resources.getDimensionPixelOffset(R.dimen.dp_30)
        })

        val test3 = FrameLayout(this).apply {
            setBackgroundResource(R.color.teal_200)
        }
        container.addView(test3, FrameLayout.LayoutParams(resources.getDimensionPixelOffset(R.dimen.dp_50), resources.getDimensionPixelOffset(R.dimen.dp_50)).apply {
            gravity = Gravity.CENTER
            marginEnd = resources.getDimensionPixelOffset(R.dimen.dp_10)
            bottomMargin = resources.getDimensionPixelOffset(R.dimen.dp_10)
        })
    }

}