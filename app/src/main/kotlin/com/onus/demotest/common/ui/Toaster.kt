package com.onus.demotest.common.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.onus.demotest.R

/**
 * @Author: onuszhao
 * @Date: 2025-01-22 16:42
 * @Description:
 */
object Toaster {

    private val handler = Handler(Looper.getMainLooper())

    fun show(context: Context, text: String, duration: Int = 2500) {
        handler.post {
            showCustomView(context, text, duration)
        }
    }

    fun show(context: Context, text: String, duration: Int = 2500, action: String, clickListener: View.OnClickListener) {
        handler.post {
            showCustomView(context, text, duration, action, clickListener)
        }
    }

    private fun showCustomView(context: Context, tips: String, duration: Int, action: String? = null, clickListener: OnClickListener? = null) {
        val container = LinearLayout(context).apply {
            isClickable = true
            isFocusable = true
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#FF222222"))
            minimumHeight = context.resources.getDimensionPixelOffset(R.dimen.dp_43)
        }
        val tipsView = TextView(context).apply {
            text = tips
            textSize = context.resources.getDimension(R.dimen.dp_10)
            setTextColor(Color.parseColor("#FFFFFFFF"))
        }
        container.addView(tipsView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            weight = 1f
            marginStart = context.resources.getDimensionPixelOffset(R.dimen.dp_18)
        })

        if (!action.isNullOrEmpty()) {
            val actionView = TextView(context).apply {
                isClickable = true
                isFocusable = true
                text = action
                textSize = context.resources.getDimension(R.dimen.dp_10)
                setTextColor(Color.parseColor("#FFFFE300"))
                setOnClickListener {
                    Log.d("onuszhao", "click action")
                    clickListener?.onClick(it)
                }
            }
            container.addView(
                actionView,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = context.resources.getDimensionPixelOffset(R.dimen.dp_24)
                    marginEnd = context.resources.getDimensionPixelOffset(R.dimen.dp_18)
                })

        }

        val toast = Toast.makeText(context, "", duration)
        toast.view = container
        toast.setGravity(Gravity.BOTTOM, 0, context.resources.getDimensionPixelOffset(R.dimen.dp_120))
        toast.show()
    }

    fun showSnackBar(context: Context, view: View, text: String, duration: Int = 2500) {
        handler.post {
            showCustomSnackBar(context, view, text, duration)
        }
    }

    fun showSnackBar(context: Context, view: View, text: String, duration: Int = 2500, action: String, clickListener: View.OnClickListener) {
        handler.post {
            showCustomSnackBar(context, view, text, duration, action, clickListener)
        }
    }

    private fun showCustomSnackBar(context: Context, view: View, tips: String, duration: Int, action: String? = null, clickListener: OnClickListener? = null) {
        val snackbar = Snackbar.make(view, tips, duration)
        snackbar.setText(tips)

        val snackbarView = snackbar.view

        // 自适应，且居下(至少com.google.android.material:material:1.6.1才有效)
        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.marginStart = context.resources.getDimensionPixelOffset(R.dimen.dp_18)
        params.marginEnd = context.resources.getDimensionPixelOffset(R.dimen.dp_18)
        params.bottomMargin = context.resources.getDimensionPixelOffset(R.dimen.dp_220)
        snackbarView.layoutParams = params

        // 居中对齐
        val screenWidth = view.resources.displayMetrics.widthPixels
        val screenHeight = view.resources.displayMetrics.heightPixels
        Log.d("onuszhao", "screenWidth=$screenWidth  screenHeight=$screenHeight")
        // snackbarView.translationX = (screenWidth / 2 - snackbarView.measuredWidth / 2).toFloat()

        // 设置圆角背景
        snackbarView.background = ContextCompat.getDrawable(view.context, R.drawable.snackbar_bg)
        snackbarView.setPaddingRelative(0, 0, 0, 0)

        if (!action.isNullOrEmpty()) {
            snackbar.setAction(action, clickListener)
            snackbar.setActionTextColor(Color.parseColor("#FFFFE300"))
        }

        snackbar.show()
    }
}