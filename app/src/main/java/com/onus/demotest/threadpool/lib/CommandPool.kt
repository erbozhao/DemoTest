package com.onus.demotest.threadpool.lib

import java.util.ArrayList
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

open class CommandPool(
    maximumPoolSize: Int,
    private val priority: Int
) : AbstractExecutorService(), ICommandPool {
    companion object {
        private const val RUNNING = 0
        private const val SHUT_DOWN = 1
        private const val TERMINATED = 2
    }

    private var executor: CommandThreadPoolExecutor? = null
    protected var waitingCommands: Queue<Runnable> = LinkedList()
    @Volatile
    private var maximumPoolSize: Int
    protected val runningCount = AtomicInteger(0)
    protected val commandLock = ReentrantLock()
    protected val termination: Condition = commandLock.newCondition()
    protected val state = AtomicInteger(RUNNING)
    private var performanceMonitor = false
    protected var commandListenerRef: ICommandListener? = null
    protected val queueTimeCaches = ConcurrentHashMap<Int, Long>()
    protected var systemTimeProviderRef: SystemTimeProvider? = null
    protected val maxQueueCount = AtomicInteger(0)
    protected val completedTaskCount = AtomicInteger(0)
    protected var nameValue: String? = null
    protected var executionHandlerRef: CommandRejectedExecutionHandler? = null
    protected var runnableListenerRef: RunnableListener? = null
    protected var maxRunningCountValue = 0

    init {
        if (maximumPoolSize <= 0) {
            throw IllegalArgumentException()
        }
        this.maximumPoolSize = maximumPoolSize
    }

    fun setSystemTimeProvider(systemTimeProvider: SystemTimeProvider?) {
        this.systemTimeProviderRef = systemTimeProvider
    }

    fun enablePerformanceMonitor(performanceMonitor: Boolean) {
        this.performanceMonitor = performanceMonitor
    }

    fun isPerformanceMonitor(): Boolean {
        return performanceMonitor
    }

    protected fun updateMaxPoolSize(newMaxPoolSize: Int): Boolean {
        if (maximumPoolSize != newMaxPoolSize) {
            maximumPoolSize = newMaxPoolSize
            return true
        }
        return false
    }

    fun setExecutor(executor: CommandThreadPoolExecutor?) {
        this.executor = executor
        this.executor?.registerCommandPool(this)
    }

    override fun shutdown() {
        state.compareAndSet(RUNNING, SHUT_DOWN)
        tryTerminate()
    }

    override fun shutdownNow(): MutableList<Runnable> {
        val lists = ArrayList<Runnable>()
        commandLock.lock()
        try {
            lists.addAll(waitingCommands)
            waitingCommands.clear()
        } catch (_: OutOfMemoryError) {
        } finally {
            commandLock.unlock()
        }
        shutdown()
        return lists
    }

    override fun isShutdown(): Boolean {
        return state.get() != RUNNING
    }

    override fun isTerminated(): Boolean {
        return state.get() == TERMINATED
    }

    @Throws(InterruptedException::class)
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        var nanos = unit.toNanos(timeout)
        commandLock.lock()
        try {
            while (!isTerminated) {
                if (nanos <= 0L) {
                    return false
                }
                nanos = termination.awaitNanos(nanos)
            }
        } finally {
            commandLock.unlock()
        }
        return true
    }

    fun setCommandQueue(queue: Queue<Runnable>) {
        waitingCommands = queue
    }

    fun setCommandListener(commandListener: ICommandListener?) {
        this.commandListenerRef = commandListener
    }

    fun setName(name: String?) {
        this.nameValue = name
    }

    fun cancelAll() {
        commandLock.lock()
        try {
            waitingCommands.clear()
        } finally {
            commandLock.unlock()
        }
    }

    fun remove(runnable: Runnable): Boolean {
        commandLock.lock()
        try {
            if (waitingCommands.remove(runnable)) {
                return true
            }
        } finally {
            commandLock.unlock()
        }
        return false
    }

    fun updatePriority(runnable: Runnable): Boolean {
        if (remove(runnable)) {
            execute(runnable)
            return true
        }
        return false
    }

    fun setExecutionHandler(executionHandler: CommandRejectedExecutionHandler?) {
        this.executionHandlerRef = executionHandler
    }

    fun setRunnableListener(runnableListener: RunnableListener?) {
        this.runnableListenerRef = runnableListener
    }

    fun getMaxRunningCount(): Int {
        return maxRunningCountValue
    }

    fun getCompletedTaskCount(): Int {
        return completedTaskCount.get()
    }

    fun getMaximumPoolSize(): Int {
        return maximumPoolSize
    }

    fun setMaximumPoolSize(maximumPoolSize: Int) {
        if (maximumPoolSize <= 0) {
            throw IllegalArgumentException()
        }
        if (updateMaxPoolSize(maximumPoolSize)) {
            executor?.updateMaximumPoolSize()
        }
    }

    fun getMaxQueueCount(): Int {
        return maxQueueCount.get()
    }

    protected fun tryTerminate() {
        commandLock.lock()
        try {
            if (waitingCommands.isEmpty() && state.get() == SHUT_DOWN && runningCount.get() == 0) {
                state.set(TERMINATED)
                executor?.unRegisterCommandPool(this)
                termination.signalAll()
            }
        } finally {
            commandLock.unlock()
        }
    }

    override fun execute(command: Runnable) {
        if (isShutdown) {
            rejectCommand(command)
            return
        }

        var waitingCmd: Command? = null
        commandLock.lock()
        try {
            cacheQueueTime(command)
            if (!isRunningMaxSize()) {
                if (waitingCommands.isEmpty()) {
                    waitingCmd = createCmd(command)
                } else {
                    val offerSuccess = waitingCommands.offer(command)
                    waitingCmd = createCmd(waitingCommands.poll())
                    if (!offerSuccess && !waitingCommands.offer(command)) {
                        rejectCommand(command)
                    }
                }
            } else {
                if (!waitingCommands.offer(command)) {
                    rejectCommand(command)
                }
            }
        } finally {
            commandLock.unlock()
        }

        if (waitingCmd != null) {
            executor?.execute(waitingCmd)
        } else {
            checkNotifyQueueSize()
        }
    }

    override fun takeWaitingCommand(): Command? {
        commandLock.lock()
        try {
            if (!isRunningMaxSize()) {
                return createCmd(waitingCommands.poll())
            }
        } finally {
            commandLock.unlock()
        }
        return null
    }

    override fun getMaximumThreadSize(): Int {
        return maximumPoolSize
    }

    override fun onCmdExecuted(command: Command) {
        decrementRunningCount()
        completedTaskCount.incrementAndGet()
    }

    override fun onReject(command: Command) {
        decrementRunningCount()
        rejectCommand(command.runnable)
    }

    override fun getName(): String? {
        return nameValue
    }

    override fun beforeExecute(command: Command) {
        notifyBeforeExecute(command)
        runnableListenerRef?.beforeExecute(command.runnable)
    }

    override fun afterExecute(command: Command) {
        notifyAfterExecute(command)
        runnableListenerRef?.afterExecute(command.runnable)
    }

    override fun compareTo(other: ICommandPool): Int {
        val otherPriority = (other as CommandPool).priority
        return when {
            priority < otherPriority -> -1
            priority > otherPriority -> 1
            else -> 0
        }
    }

    override fun toString(): String {
        return buildString {
            append(nameValue)
            append("[")
            append(", maximumPoolSize = ")
            append(maximumPoolSize)
            append(", waitingCommandSize = ")
            append(waitingCommands.size)
            append(", runningCount = ")
            append(runningCount.get())
            append(", completed = ")
            append(completedTaskCount.get())
            append(", maxQueueCount = ")
            append(maxQueueCount.get())
            append(", maxRunningCount = ")
            append(maxRunningCountValue)
            append("]")
        }
    }

    private fun rejectCommand(runnable: Runnable) {
        executionHandlerRef?.rejectedExecution(runnable, this)
    }

    private fun cacheQueueTime(runnable: Runnable) {
        if (performanceMonitor) {
            val systemTimeProvider = systemTimeProviderRef ?: return
            queueTimeCaches[runnable.hashCode()] = systemTimeProvider.currentTime()
        }
    }

    private fun checkNotifyQueueSize() {
        if (performanceMonitor && commandListenerRef != null) {
            val waitingCmdSize = waitingCommands.size
            if (waitingCmdSize > maxQueueCount.get()) {
                maxQueueCount.set(waitingCmdSize)
                commandListenerRef?.onQueueMaxSizeChanged(this, maxQueueCount.get())
            }
        }
    }

    private fun createCmd(runnable: Runnable?): Command? {
        if (runnable != null) {
            val newRunningCount = runningCount.incrementAndGet()
            maxRunningCountValue = maxOf(newRunningCount, maxRunningCountValue)
            return Command(runnable, this, priority)
        }
        return null
    }

    private fun isRunningMaxSize(): Boolean {
        return runningCount.get() >= maximumPoolSize
    }

    private fun decrementRunningCount() {
        runningCount.decrementAndGet()
        tryTerminate()
    }

    private fun notifyBeforeExecute(command: Command) {
        if (performanceMonitor) {
            val queueTime = queueTimeCaches.remove(command.runnable.hashCode())
            if (queueTime != null) {
                val systemTimeProvider = systemTimeProviderRef ?: return
                command.recordQueueTime(queueTime)
                command.recordStartExeTime(systemTimeProvider.currentTime())
                commandListenerRef?.onCmdStart(this, command)
            }
        }
    }

    private fun notifyAfterExecute(command: Command) {
        if (performanceMonitor) {
            val systemTimeProvider = systemTimeProviderRef ?: return
            command.recordCompletedTime(systemTimeProvider.currentTime())
            commandListenerRef?.onCmdCompleted(this, command)
        }
    }
}
