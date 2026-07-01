package com.onus.demotest.common

import android.content.Context
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import java.lang.reflect.Field
import java.util.Objects
import kotlin.math.roundToInt

/**
 * @author onuszhao
 * @since 2025/12/2
 * @description
 */

object DeviceUtils {

    private var statusBarHeight: Int = -1

    fun getStatusBarHeight(context: Context): Int {
        if (statusBarHeight > 0) {
            return statusBarHeight
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics = wm.getCurrentWindowMetrics()
            val windowInsets = windowMetrics.getWindowInsets()
            val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars() or WindowInsets.Type.displayCutout())
            statusBarHeight = insets.top
            return statusBarHeight
        } else {
            return getStatusBarHeightFromSystem(context)
        }
    }

    private fun getStatusBarHeightFromSystem(context: Context): Int {
        if (statusBarHeight > 0) {
            return statusBarHeight
        }

        var c: Class<*>? = null
        var obj: Any? = null
        var field: Field? = null
        var x = 0
        try {
            c = Class.forName("com.android.internal.R\$dimen")
            obj = c.newInstance()
            field = c.getField("status_bar_height")
            x = Objects.requireNonNull<Any?>(field.get(obj)).toString().toInt()
            statusBarHeight = context.resources.getDimensionPixelSize(x)
        } catch (e1: Exception) {
            statusBarHeight = -1
            e1.printStackTrace()
        }

        if (statusBarHeight < 1) {
            try {
                val statebarH_id: Int = context.resources.getIdentifier(
                    "statebar_height", "dimen",
                    context.packageName
                )
                statusBarHeight = context.resources.getDimension(statebarH_id).roundToInt()
            } catch (e2: Exception) {
                statusBarHeight = 0
            }
        }
        return statusBarHeight
    }

    private var naviBarHeight: Int = -1

    fun getSystemNaviBarHeight(context: Context): Int {
        if (naviBarHeight != -1) {
            return naviBarHeight
        }
        naviBarHeight = getNavigationBarHeight(context)
        return naviBarHeight
    }

    private fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        try {
            return resources.getDimensionPixelSize(resourceId)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0
    }
}