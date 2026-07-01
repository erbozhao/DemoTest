package com.onus.demotest.threadpool.lib

import android.os.SystemClock

open class SystemTimeProvider {
    open fun currentTime(): Long {
        return SystemClock.elapsedRealtime()
    }
}
