package com.onus.demotest.pages.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.utils.DeviceUtils

/**
 * @Author: onuszhao
 * @Date: 2024-10-18 14:27
 * @Description:
 */
class FontsActivity: AppCompatActivity() {


    private lateinit var container: ScrollView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        container = ScrollView(this).apply {
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@FontsActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTheme(R.style.ActivityMain_Light)
        } else {
            setTheme(R.style.ActivityMain)
        }

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            container.addView(this, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ))
        }

        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! normal"
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! medium"
            paint.isFakeBoldText = true
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! bold"
            // 调整描边宽度实现medium
            paint.strokeWidth = 2f
            paint.style = Paint.Style.FILL_AND_STROKE
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! italic"
            paint.textSkewX = -0.3f // 负值倾斜
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! italic bold"
            paint.textSkewX = -0.3f // 负值倾斜
            paint.strokeWidth = 2f
            paint.style = Paint.Style.FILL_AND_STROKE
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT"
            typeface = Typeface.DEFAULT
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT_BOLD"
            typeface = Typeface.DEFAULT_BOLD
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! MONOSPACE"
            typeface = Typeface.MONOSPACE
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! SANS_SERIF"
            typeface = Typeface.SANS_SERIF
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! SERIF"
            typeface = Typeface.SERIF
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT ITALIC"
            setTypeface(Typeface.DEFAULT, Typeface.ITALIC)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT_BOLD ITALIC"
            setTypeface(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT BOLD_ITALIC"
            setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT NORMAL"
            setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT BOLD"
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=100"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 100, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=200"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 200, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=300"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 300, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=400"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 400, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=500"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 500, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=600"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 600, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=700"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 700, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=800"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 800, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=900"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 900, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! DEFAULT weight=1000"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                typeface = Typeface.create(Typeface.DEFAULT, 1000, false)
            }
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-thin"
            typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-light"
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif"
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif bold"
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-medium"
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-black"
            typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-condensed-light"
            typeface = Typeface.create("sans-serif-condensed-light", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-condensed"
            typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-condensed-medium"
            typeface = Typeface.create("sans-serif-condensed-medium", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-condensed bold"
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! serif"
            typeface = Typeface.create("serif", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! serif bold"
            typeface = Typeface.create("serif", Typeface.BOLD)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! monospace"
            typeface = Typeface.create("monospace", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! casual"
            typeface = Typeface.create("casual", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! cursive"
            typeface = Typeface.create("cursive", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! cursive"
            typeface = Typeface.create("cursive", Typeface.BOLD)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! sans-serif-smallcaps"
            typeface = Typeface.create("sans-serif-smallcaps", Typeface.BOLD)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
        wrapper.addView(TextView(this).apply {
            text = "This is a test Text!!! roboto-medium"
            typeface = Typeface.create("roboto-medium", Typeface.NORMAL)
            textSize = resources.getDimension(R.dimen.dp_4)
            setTextColor(resources.getColor(R.color.black))
        })
    }

}