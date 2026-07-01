package com.onus.demotest.threadpool

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

class CoreHandlerThread private constructor() : HandlerThread("CoreHandlerThread") {
    private val handler: Handler

    init {
        start()
        handler = Handler(looper)
    }

    override fun getLooper(): Looper {
        return super.getLooper()
    }

    fun post(runnable: Runnable) {
        handler.post(runnable)
    }

    fun postDelayed(runnable: Runnable, delayMillis: Long) {
        handler.postDelayed(runnable, delayMillis)
    }

    fun removeCallbacks(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }

    companion object {
        @Volatile
        private var sInstance: CoreHandlerThread? = null

        @JvmStatic
        fun getInstance(): CoreHandlerThread {
            if (sInstance == null) {
                synchronized(CoreHandlerThread::class.java) {
                    if (sInstance == null) {
                        sInstance = CoreHandlerThread()
                    }
                }
            }
            return sInstance!!
        }
    }
}
