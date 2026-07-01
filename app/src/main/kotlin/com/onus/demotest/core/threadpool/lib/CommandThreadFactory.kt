package com.onus.demotest.core.threadpool.lib

import android.os.Process
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

open class CommandThreadFactory @JvmOverloads constructor(
    private val threadPoolName: String,
    private val priority: Int = Process.THREAD_PRIORITY_DEFAULT
) : ThreadFactory {
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable): Thread {
        return object : Thread(r, getThreadName()) {
            override fun run() {
                try {
                    Process.setThreadPriority(priority)
                } catch (_: Throwable) {
                }
                super.run()
            }
        }
    }

    protected open fun getThreadName(): String {
        return "TP-$threadPoolName-${threadNumber.getAndIncrement()}"
    }
}
