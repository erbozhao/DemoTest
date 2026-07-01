package com.onus.demotest.threadpool.lib

import android.os.Process
import java.util.Comparator
import java.util.Queue
import java.util.concurrent.Executor
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

open class CommandPoolSupplier : IWorkerListener, ICommandListener {
    private var corePoolSize: Int = 1
    private var keepAliveTime: Long = 60
    private var timeUnit: TimeUnit = TimeUnit.SECONDS
    private var threadFactory: ThreadFactory = CommandThreadFactory("command_thread")

    @Volatile
    private var executor: CommandThreadPoolExecutor? = null
    private val executorLock = Any()
    private var timeProvider: SystemTimeProvider? = null
    private var enablePerformanceMonitor: Boolean = false
    private val commandPoolCount = AtomicInteger(0)
    private var commandExeListener: ICommandListener? = null
    private var workerListener: IWorkerListener? = null

    fun setCommandListener(commandExeListener: ICommandListener?) {
        this.commandExeListener = commandExeListener
    }

    fun setWorkerListener(workerListener: IWorkerListener?) {
        this.workerListener = workerListener
    }

    fun enablePerformanceMonitor(enablePerformanceMonitor: Boolean) {
        this.enablePerformanceMonitor = enablePerformanceMonitor
    }

    fun init(corePoolSize: Int, keepAliveTime: Long, unit: TimeUnit?, threadFactory: ThreadFactory?) {
        if (corePoolSize > 0) {
            this.corePoolSize = corePoolSize
        }
        if (keepAliveTime > 0) {
            this.keepAliveTime = keepAliveTime
        }
        if (unit != null) {
            this.timeUnit = unit
        }
        if (threadFactory != null) {
            this.threadFactory = threadFactory
        }
    }

    private fun initExecutor() {
        if (executor == null) {
            synchronized(executorLock) {
                if (executor == null) {
                    executor = CommandThreadPoolExecutor(
                        corePoolSize,
                        keepAliveTime,
                        timeUnit,
                        PriorityBlockingQueue(11, Comparator { o1: Command, o2: Command ->
                            if (o1 is Comparable<*> && o2 is Comparable<*>) {
                                @Suppress("UNCHECKED_CAST")
                                (o1 as Comparable<Any>).compareTo(o2)
                            } else {
                                0
                            }
                        })
                    )
                    executor?.setThreadFactory(threadFactory)
                }
                if (enablePerformanceMonitor) {
                    executor?.setWorkerListener(this)
                }
            }
        }
    }

    @JvmOverloads
    fun applyCommandPool(maximumPoolSize: Int, priority: Int = Process.THREAD_PRIORITY_DEFAULT, name: String? = null): CommandPool {
        initExecutor()
        commandPoolCount.incrementAndGet()
        val commandPool = CommandPool(maximumPoolSize, priority)
        commandPool.setExecutor(executor)
        val resolvedName = if (name.isNullOrEmpty()) createDefaultName() else name
        commandPool.setName(resolvedName)
        initTimeProvider()
        commandPool.setSystemTimeProvider(timeProvider)
        if (enablePerformanceMonitor) {
            commandPool.setCommandListener(this)
            commandPool.enablePerformanceMonitor(true)
        }
        return commandPool
    }

    fun newFixedThreadPool(nThreads: Int, name: String): Executor {
        return applyCommandPool(nThreads, name = name)
    }

    private fun initTimeProvider() {
        if (timeProvider == null) {
            timeProvider = SystemTimeProvider()
        }
    }

    fun setTimeProvider(timeProvider: SystemTimeProvider?) {
        this.timeProvider = timeProvider
    }

    fun getExecutor(): CommandThreadPoolExecutor? {
        initExecutor()
        return executor
    }

    override fun onWorkerAdded(workerId: String) {
        workerListener?.onWorkerAdded(workerId)
    }

    override fun onWorkerExited(workerId: String) {
        workerListener?.onWorkerExited(workerId)
    }

    private fun createDefaultName(): String {
        return "CommandPool_${commandPoolCount.get()}"
    }

    override fun onCmdStart(commandPool: CommandPool, command: Command) {
        commandExeListener?.onCmdStart(commandPool, command)
    }

    override fun onCmdCompleted(commandPool: CommandPool, command: Command) {
        commandExeListener?.onCmdCompleted(commandPool, command)
    }

    override fun onQueueMaxSizeChanged(commandPool: CommandPool, maxQueueSize: Int) {
        commandExeListener?.onQueueMaxSizeChanged(commandPool, maxQueueSize)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("CommandPoolSupplier:\n")
        val commandPools: Queue<ICommandPool>? = executor?.getCommandPools()
        if (executor != null) {
            sb.append("    ").append(executor.toString()).append("\n")
        }
        if (commandPools != null) {
            val cmdPools = commandPools.iterator()
            while (cmdPools.hasNext()) {
                sb.append("    ").append(cmdPools.next().toString()).append("\n")
            }
        }
        return sb.toString()
    }

    fun shutdown() {
        executor?.shutdown()
    }

    fun shutdownNow(): List<Command> {
        return executor?.shutdownNow() ?: emptyList()
    }
}
