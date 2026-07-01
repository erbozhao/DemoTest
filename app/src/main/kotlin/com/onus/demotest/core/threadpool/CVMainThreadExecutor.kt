package com.onus.demotest.core.threadpool

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

open class CVMainThreadExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        handler.post(command)
    }

    fun execute(command: Runnable, delay: Long) {
        handler.postDelayed(command, delay)
    }

    fun remove(command: Runnable) {
        handler.removeCallbacks(command)
    }
}
