package com.onus.demotest.threadpool

import android.util.Log
import com.onus.demotest.threadpool.lib.CommandPool
import com.onus.demotest.threadpool.lib.CommandRejectedExecutionHandler
import com.onus.demotest.threadpool.lib.CommandServiceManager
import com.onus.demotest.threadpool.lib.ICommandListener
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.RunnableFuture
import java.util.concurrent.TimeUnit

open class CVThreadPoolExecutor(
    maximumPoolSize: Int,
    priority: POOL_PRIORITY,
    workQueue: BlockingQueue<Runnable>,
    handler: CVRejectedExecutionHandler
) : AbstractExecutorService(), CVExecutorService {
    enum class POOL_PRIORITY {
        URGENT_DISPLAY,
        DISPLAY,
        BACKGROUND
    }

    private var mCommandPool: CommandPool
    private var mWorkQueue: BlockingQueue<Runnable>

    init {
        mWorkQueue = workQueue
        mCommandPool = CommandServiceManager.get().getCommandSupplier()
            .applyCommandPool(maximumPoolSize, priority.ordinal)
        mCommandPool.setCommandQueue(workQueue)
        mCommandPool.setExecutionHandler(CommandExecutionHandler(this, handler))
    }

    constructor(maximumPoolSize: Int, workQueue: BlockingQueue<Runnable>) : this(
        maximumPoolSize,
        POOL_PRIORITY.BACKGROUND,
        workQueue,
        defaultHandler
    )

    constructor(maximumPoolSize: Int, priority: POOL_PRIORITY, workQueue: BlockingQueue<Runnable>) : this(
        maximumPoolSize,
        priority,
        workQueue,
        defaultHandler
    )

    fun setCommandListener(commandListener: ICommandListener?) {
        mCommandPool.setCommandListener(commandListener)
    }

    fun enablePerformanceMonitor(performanceMonitor: Boolean) {
        mCommandPool.enablePerformanceMonitor(performanceMonitor)
    }

    override fun shutdown() {
        mCommandPool.shutdown()
    }

    override fun shutdownNow(): List<Runnable> {
        return mCommandPool.shutdownNow()
    }

    override fun isShutdown(): Boolean {
        return mCommandPool.isShutdown
    }

    override fun isTerminated(): Boolean {
        return mCommandPool.isTerminated
    }

    @Throws(InterruptedException::class)
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        return mCommandPool.awaitTermination(timeout, unit)
    }

    override fun execute(command: Runnable) {
        if (mCommandPool.isPerformanceMonitor()) {
            mCommandPool.execute(PerformanceRunnable(command))
        } else {
            mCommandPool.execute(command)
        }
    }

    override fun <T> newTaskFor(callable: Callable<T>): RunnableFuture<T> {
        return CVFutureTask(callable)
    }

    override fun <T> newTaskFor(runnable: Runnable, value: T): RunnableFuture<T> {
        return CVFutureTask(runnable, value)
    }

    override fun remove(command: Runnable): Boolean {
        return mCommandPool.remove(command)
    }

    override fun getQueue(): BlockingQueue<Runnable> {
        return mWorkQueue
    }

    private class CommandExecutionHandler(
        private val mExecutor: CVThreadPoolExecutor,
        private val mHandler: CVRejectedExecutionHandler?
    ) : CommandRejectedExecutionHandler {
        override fun rejectedExecution(r: Runnable, commandPool: CommandPool) {
            mHandler?.rejectedExecution(r, mExecutor)
        }
    }

    companion object {
        @JvmField
        val defaultHandler: CVRejectedExecutionHandler = object : CVRejectedExecutionHandler {
            override fun rejectedExecution(r: Runnable, executor: CVThreadPoolExecutor) {
                Log.d("ExecutionHandler", "rejectedExecution: $r, executor: $executor")
            }
        }
    }
}
