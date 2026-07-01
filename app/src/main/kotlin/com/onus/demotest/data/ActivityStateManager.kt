package com.onus.demotest.data

import android.annotation.SuppressLint
import android.app.Activity

/**
 * @Author: onuszhao
 * @Date: 2023-10-12 17:19
 * @Description:
 */
@SuppressLint("StaticFieldLeak")
object ActivityStateManager {
    private var curActivity: Activity? = null

    fun setCurActivity(activity: Activity) {
        curActivity = activity
    }

    fun getCurActivity(): Activity? {
        return curActivity
    }

    fun release() {
        curActivity = null
    }
}