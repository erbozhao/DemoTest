package com.onus.demotest.threadpool.lib

import java.util.concurrent.locks.AbstractQueuedSynchronizer

class Worker(
    private val commandThreadPoolExecutor: CommandThreadPoolExecutor,
    @JvmField var firstTask: Command?
) : AbstractQueuedSynchronizer(), Runnable {
    @JvmField
    val thread: Thread = commandThreadPoolExecutor.getThreadFactory().newThread(this)

    @Volatile
    @JvmField
    var completedTasks: Long = 0

    init {
        state = -1
    }

    override fun run() {
        commandThreadPoolExecutor.runWorker(this)
    }

    override fun isHeldExclusively(): Boolean {
        return state != 0
    }

    override fun tryAcquire(unused: Int): Boolean {
        return if (compareAndSetState(0, 1)) {
            exclusiveOwnerThread = Thread.currentThread()
            true
        } else {
            false
        }
    }

    override fun tryRelease(unused: Int): Boolean {
        exclusiveOwnerThread = null
        state = 0
        return true
    }

    fun lock() {
        acquire(1)
    }

    fun tryLock(): Boolean {
        return tryAcquire(1)
    }

    fun unlock() {
        release(1)
    }

    fun isLocked(): Boolean {
        return isHeldExclusively
    }

    fun interruptIfStarted() {
        val currentThread = thread
        if (state >= 0 && !currentThread.isInterrupted) {
            try {
                currentThread.interrupt()
            } catch (_: SecurityException) {
            }
        }
    }

    companion object {
        private const val serialVersionUID: Long = 6138294804551838833L
    }
}
