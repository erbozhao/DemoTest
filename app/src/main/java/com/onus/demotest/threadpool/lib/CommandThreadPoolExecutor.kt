package com.onus.demotest.threadpool.lib

import android.util.Log
import java.util.ArrayList
import java.util.HashSet
import java.util.PriorityQueue
import java.util.Queue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

open class CommandThreadPoolExecutor(
    corePoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    private val workQueue: BlockingQueue<Command>
) {
    companion object {
        private const val COUNT_BITS = Int.SIZE_BITS - 3
        private const val CAPACITY = (1 shl COUNT_BITS) - 1
        private const val RUNNING = -1 shl COUNT_BITS
        private const val SHUTDOWN = 0 shl COUNT_BITS
        private const val STOP = 1 shl COUNT_BITS
        private const val TIDYING = 2 shl COUNT_BITS
        private const val TERMINATED = 3 shl COUNT_BITS
        private val shutdownPerm = RuntimePermission("modifyThread")
        private const val ONLY_ONE = true

        private fun runStateOf(c: Int): Int {
            return c and CAPACITY.inv()
        }

        private fun workerCountOf(c: Int): Int {
            return c and CAPACITY
        }

        private fun ctlOf(rs: Int, wc: Int): Int {
            return rs or wc
        }

        private fun runStateLessThan(c: Int, s: Int): Boolean {
            return c < s
        }

        private fun runStateAtLeast(c: Int, s: Int): Boolean {
            return c >= s
        }

        private fun isRunning(c: Int): Boolean {
            return c < SHUTDOWN
        }
    }

    val commandPoolsLock = Any()
    private val ctl = AtomicInteger(ctlOf(RUNNING, 0))
    private val waitWorkerCount = AtomicInteger(0)
    private val mainLock = ReentrantLock()
    private val workers = HashSet<Worker>()
    private val termination: Condition = mainLock.newCondition()
    private var commandPools = PriorityQueue<ICommandPool>()
    private var workerListener: IWorkerListener? = null
    private var largestPoolSize = 0
    private var completedTaskCount = 0L
    @Volatile
    private var threadFactory: ThreadFactory
    @Volatile
    private var keepAliveTimeNanos: Long
    @Volatile
    private var allowCoreThreadTimeOut = false
    @Volatile
    private var corePoolSizeValue: Int
    @Volatile
    private var maximumPoolSizeValue: Int

    init {
        if (keepAliveTime < 0 || corePoolSize <= 0) {
            throw IllegalArgumentException()
        }
        this.corePoolSizeValue = corePoolSize
        this.keepAliveTimeNanos = unit.toNanos(keepAliveTime)
        this.threadFactory = Executors.defaultThreadFactory()
        this.maximumPoolSizeValue = corePoolSize
    }

    private fun compareAndIncrementWorkerCount(expect: Int): Boolean {
        return ctl.compareAndSet(expect, expect + 1)
    }

    private fun compareAndDecrementWorkerCount(expect: Int): Boolean {
        return ctl.compareAndSet(expect, expect - 1)
    }

    private fun decrementWorkerCount() {
        do {
        } while (!compareAndDecrementWorkerCount(ctl.get()))
    }

    fun setWorkerListener(workerListener: IWorkerListener?) {
        this.workerListener = workerListener
    }

    fun getThreadFactory(): ThreadFactory {
        return threadFactory
    }

    fun setThreadFactory(threadFactory: ThreadFactory) {
        this.threadFactory = threadFactory
    }

    fun getCorePoolSize(): Int {
        return corePoolSizeValue
    }

    fun setCorePoolSize(corePoolSize: Int) {
        if (corePoolSize < 0) {
            throw IllegalArgumentException()
        }
        val delta = corePoolSize - corePoolSizeValue
        corePoolSizeValue = corePoolSize
        if (workerCountOf(ctl.get()) > corePoolSize) {
            interruptIdleWorkers()
        } else if (delta > 0) {
            var k = minOf(delta, workQueue.size)
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty()) {
                    break
                }
            }
        }
    }

    fun prestartCoreThread(): Boolean {
        return workerCountOf(ctl.get()) < corePoolSizeValue && addWorker(null, true)
    }

    fun ensurePrestart() {
        val wc = workerCountOf(ctl.get())
        if (wc < corePoolSizeValue) {
            addWorker(null, true)
        } else if (wc == 0) {
            addWorker(null, false)
        }
    }

    fun prestartAllCoreThreads(): Int {
        var n = 0
        while (addWorker(null, true)) {
            ++n
        }
        return n
    }

    fun allowsCoreThreadTimeOut(): Boolean {
        return allowCoreThreadTimeOut
    }

    fun allowCoreThreadTimeOut(value: Boolean) {
        if (value && keepAliveTimeNanos <= 0) {
            throw IllegalArgumentException("Core threads must have nonzero keep alive times")
        }
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value
            if (value) {
                interruptIdleWorkers()
            }
        }
    }

    fun getMaximumPoolSize(): Int {
        return maximumPoolSizeValue
    }

    private fun setMaximumPoolSize(maximumPoolSize: Int) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSizeValue) {
            throw IllegalArgumentException()
        }
        maximumPoolSizeValue = maximumPoolSize
        if (workerCountOf(ctl.get()) > maximumPoolSize) {
            interruptIdleWorkers()
        }
    }

    fun setKeepAliveTime(time: Long, unit: TimeUnit) {
        if (time < 0) {
            throw IllegalArgumentException()
        }
        if (time == 0L && allowsCoreThreadTimeOut()) {
            throw IllegalArgumentException("Core threads must have nonzero keep alive times")
        }
        val keepAliveTime = unit.toNanos(time)
        val delta = keepAliveTime - keepAliveTimeNanos
        keepAliveTimeNanos = keepAliveTime
        if (delta < 0) {
            interruptIdleWorkers()
        }
    }

    fun getKeepAliveTime(unit: TimeUnit): Long {
        return unit.convert(keepAliveTimeNanos, TimeUnit.NANOSECONDS)
    }

    fun registerCommandPool(commandPool: ICommandPool) {
        if (commandPool.getMaximumThreadSize() <= 0) {
            throw IllegalArgumentException()
        }
        synchronized(commandPoolsLock) {
            commandPools.offer(commandPool)
        }
        updateMaximumPoolSize()
    }

    fun unRegisterCommandPool(commandPool: ICommandPool) {
        synchronized(commandPoolsLock) {
            if (commandPools.remove(commandPool)) {
                updateMaximumPoolSize()
            }
        }
    }

    fun updateMaximumPoolSize() {
        synchronized(commandPoolsLock) {
            var maximumPoolSize = 0L
            val cmdProviders = commandPools.iterator()
            while (cmdProviders.hasNext()) {
                val provider = cmdProviders.next()
                maximumPoolSize += provider.getMaximumThreadSize().toLong()
            }
            val finalMaxPoolSize = minOf(maximumPoolSize, Int.MAX_VALUE.toLong()).toInt()
            setMaximumPoolSize(maxOf(corePoolSizeValue, finalMaxPoolSize))
        }
    }

    fun getFirstCommandPool(): ICommandPool? {
        val pools = synchronized(commandPoolsLock) { PriorityQueue(commandPools) }
        val cmdProviders = pools.iterator()
        return if (cmdProviders.hasNext()) cmdProviders.next() else null
    }

    fun getCommandPools(): Queue<ICommandPool> {
        return synchronized(commandPoolsLock) { PriorityQueue(commandPools) }
    }

    fun getWaitingCommand(task: Command): Command? {
        var waitingCmd: Command? = task.commandPool.takeWaitingCommand()
        if (waitingCmd == null) {
            val pools = synchronized(commandPoolsLock) { PriorityQueue(commandPools) }
            val cmdProviders = pools.iterator()
            while (cmdProviders.hasNext()) {
                val provider = cmdProviders.next()
                if (provider != task.commandPool) {
                    waitingCmd = provider.takeWaitingCommand()
                    if (waitingCmd != null) {
                        break
                    }
                }
            }
        }
        return waitingCmd
    }

    fun execute(command: Command) {
        var c = ctl.get()
        if (canCreateCoreThread(c)) {
            if (addWorker(command, true)) {
                return
            }
            c = ctl.get()
        }
        if (hasIdleWorker()) {
            if (isRunning(c) && workQueue.offer(command)) {
                if (isWorkerEmpty()) {
                    addWorker(null, false)
                }
            } else {
                executeCreate(command)
            }
        } else {
            executeCreate(command)
        }
    }

    fun shutdown() {
        mainLock.lock()
        try {
            checkShutdownAccess()
            advanceRunState(SHUTDOWN)
            interruptIdleWorkers()
            onShutdown()
        } finally {
            mainLock.unlock()
        }
        tryTerminate()
    }

    fun shutdownNow(): List<Command> {
        val tasks: List<Command>
        mainLock.lock()
        try {
            checkShutdownAccess()
            advanceRunState(STOP)
            interruptWorkers()
            tasks = drainQueue()
        } finally {
            mainLock.unlock()
        }
        tryTerminate()
        return tasks
    }

    fun isShutdown(): Boolean {
        return !isRunning(ctl.get())
    }

    fun isTerminating(): Boolean {
        val c = ctl.get()
        return !isRunning(c) && runStateLessThan(c, TERMINATED)
    }

    fun isTerminated(): Boolean {
        return runStateAtLeast(ctl.get(), TERMINATED)
    }

    @Throws(InterruptedException::class)
    fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        var nanos = unit.toNanos(timeout)
        mainLock.lock()
        try {
            while (!runStateAtLeast(ctl.get(), TERMINATED)) {
                if (nanos <= 0L) {
                    return false
                }
                nanos = termination.awaitNanos(nanos)
            }
            return true
        } finally {
            mainLock.unlock()
        }
    }

    protected fun finalize() {
        shutdown()
    }

    fun remove(task: Command): Boolean {
        val removed = workQueue.remove(task)
        tryTerminate()
        return removed
    }

    fun getPoolSize(): Int {
        mainLock.lock()
        try {
            return if (runStateAtLeast(ctl.get(), TIDYING)) 0 else workers.size
        } finally {
            mainLock.unlock()
        }
    }

    fun getActiveCount(): Int {
        mainLock.lock()
        try {
            var n = 0
            for (worker in workers) {
                if (worker.isLocked()) {
                    ++n
                }
            }
            return n
        } finally {
            mainLock.unlock()
        }
    }

    fun getLargestPoolSize(): Int {
        mainLock.lock()
        try {
            return largestPoolSize
        } finally {
            mainLock.unlock()
        }
    }

    fun getTaskCount(): Long {
        mainLock.lock()
        try {
            var n = completedTaskCount
            for (worker in workers) {
                n += worker.completedTasks
                if (worker.isLocked()) {
                    ++n
                }
            }
            return n + workQueue.size
        } finally {
            mainLock.unlock()
        }
    }

    fun getCompletedTaskCount(): Long {
        mainLock.lock()
        try {
            var n = completedTaskCount
            for (worker in workers) {
                n += worker.completedTasks
            }
            return n
        } finally {
            mainLock.unlock()
        }
    }

    protected open fun beforeExecute(command: Command) {
        command.commandPool.beforeExecute(command)
    }

    protected open fun afterExecute(command: Command, throwable: Throwable?) {
        command.commandPool.afterExecute(command)
    }

    protected open fun terminated() {
    }

    fun runWorker(worker: Worker) {
        val currentThread = Thread.currentThread()
        var task = worker.firstTask
        var isTaskCompleted = false
        worker.firstTask = null
        worker.unlock()
        var completedAbruptly = true
        try {
            while (task != null || getTask().also { task = it } != null) {
                worker.lock()
                isTaskCompleted = false
                if (interruptWorker(currentThread)) {
                    currentThread.interrupt()
                }
                try {
                    beforeExecute(task!!)
                    var thrown: Throwable? = null
                    try {
                        task!!.runnable.run()
                    } catch (x: RuntimeException) {
                        thrown = x
                        throw x
                    } catch (x: Error) {
                        thrown = x
                        throw x
                    } catch (x: Throwable) {
                        thrown = x
                        throw Error(x)
                    } finally {
                        afterExecute(task!!, thrown)
                    }
                } finally {
                    isTaskCompleted = true
                    worker.completedTasks++
                    task!!.commandPool.onCmdExecuted(task!!)
                    task = getWaitingCommand(task!!)
                    worker.unlock()
                }
            }
            completedAbruptly = false
        } finally {
            if (!isTaskCompleted && task != null) {
                reject(task!!)
            }
            processWorkerExit(worker, completedAbruptly)
        }
    }

    fun tryTerminate() {
        while (true) {
            val c = ctl.get()
            if (isRunning(c) || runStateAtLeast(c, TIDYING) || (runStateOf(c) == SHUTDOWN && !workQueue.isEmpty())) {
                return
            }
            if (hasWorker(c)) {
                interruptIdleWorkers(ONLY_ONE)
                return
            }
            mainLock.lock()
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated()
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0))
                        termination.signalAll()
                    }
                    return
                }
            } finally {
                mainLock.unlock()
            }
        }
    }

    override fun toString(): String {
        val completed: Long
        val workerSize: Int
        val active: Int
        mainLock.lock()
        try {
            completed = completedTaskCount
            var activeCount = 0
            workerSize = workers.size
            for (worker in workers) {
                if (worker.isLocked()) {
                    ++activeCount
                }
            }
            active = activeCount
        } finally {
            mainLock.unlock()
        }
        val c = ctl.get()
        val runState = when {
            runStateLessThan(c, SHUTDOWN) -> "Running"
            runStateAtLeast(c, TERMINATED) -> "Terminated"
            else -> "Shutting down"
        }
        return "Executor[$runState, totalWorkerSize = $workerSize, activeWorkerSize = $active, idleWorkerSize = $waitWorkerCount, queued tasks = ${workQueue.size}, completed tasks = $completed, maxPoolSize = $maximumPoolSizeValue, corePoolSize = $corePoolSizeValue]"
    }

    private fun addWorker(firstTask: Command?, core: Boolean): Boolean {
        retry@ while (true) {
            var c = ctl.get()
            val rs = runStateOf(c)
            if (noNeedAdd(firstTask, rs)) {
                return false
            }
            while (true) {
                val wc = workerCountOf(c)
                if (wc >= CAPACITY || wc >= if (core) corePoolSizeValue else maximumPoolSizeValue) {
                    return false
                }
                if (compareAndIncrementWorkerCount(c)) {
                    break@retry
                }
                c = ctl.get()
                if (runStateOf(c) != rs) {
                    continue@retry
                }
            }
        }
        return addNewWorker(firstTask)
    }

    private fun interruptIdleWorkers(onlyOne: Boolean = false) {
        mainLock.lock()
        try {
            for (worker in workers) {
                val thread = worker.thread
                if (!thread.isInterrupted && worker.tryLock()) {
                    try {
                        thread.interrupt()
                    } catch (_: SecurityException) {
                    } finally {
                        worker.unlock()
                    }
                }
                if (onlyOne) {
                    break
                }
            }
        } finally {
            mainLock.unlock()
        }
    }

    private fun addNewWorker(firstTask: Command?): Boolean {
        var workerStarted = false
        var workerAdded = false
        var worker: Worker? = null
        try {
            worker = Worker(this, firstTask)
            val thread = worker.thread
            mainLock.lock()
            try {
                val rs = runStateOf(ctl.get())
                if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                    if (thread.isAlive) {
                        throw IllegalThreadStateException()
                    }
                    workers.add(worker)
                    val size = workers.size
                    if (size > largestPoolSize) {
                        largestPoolSize = size
                    }
                    workerAdded = true
                }
            } finally {
                mainLock.unlock()
            }
            if (workerAdded) {
                try {
                    thread.start()
                    workerStarted = true
                    notifyWorkerAdd(worker)
                } catch (_: OutOfMemoryError) {
                }
            }
        } finally {
            if (!workerStarted) {
                addWorkerFailed(worker)
            }
        }
        return workerStarted
    }

    private fun notifyWorkerAdd(worker: Worker) {
        workerListener?.onWorkerAdded(worker.toString())
    }

    private fun addWorkerFailed(worker: Worker?) {
        mainLock.lock()
        try {
            if (worker != null) {
                workers.remove(worker)
            }
            decrementWorkerCount()
            tryTerminate()
        } finally {
            mainLock.unlock()
        }
    }

    private fun processWorkerExit(worker: Worker, completedAbruptly: Boolean) {
        if (completedAbruptly) {
            decrementWorkerCount()
        }
        removeWorker(worker)
        notifyWorkerExit(worker)
        tryTerminate()
        checkReplaceExitedThread(completedAbruptly)
    }

    private fun removeWorker(worker: Worker) {
        mainLock.lock()
        try {
            completedTaskCount += worker.completedTasks
            workers.remove(worker)
        } finally {
            mainLock.unlock()
        }
    }

    private fun checkReplaceExitedThread(completedAbruptly: Boolean) {
        if (completedAbruptly) {
            Log.d("ExitedThread", "Thread exit completedAbruptly")
        }
        val c = ctl.get()
        if (runStateAtLeast(c, STOP)) {
            return
        }
        if (completedAbruptly) {
            replaceThread()
        } else if (workerCountOf(c) < getMinSize()) {
            replaceThread()
        }
    }

    private fun replaceThread() {
        if (!workQueue.isEmpty()) {
            addWorker(null, false)
        }
    }

    private fun getMinSize(): Int {
        var min = if (allowCoreThreadTimeOut) 0 else corePoolSizeValue
        if (min == 0 && !workQueue.isEmpty()) {
            min = 1
        }
        return min
    }

    private fun notifyWorkerExit(worker: Worker) {
        workerListener?.onWorkerExited(worker.toString())
    }

    private fun getTask(): Command? {
        var timedOut = false
        while (true) {
            val c = ctl.get()
            val rs = runStateOf(c)
            if (shouldExitWorker(rs)) {
                decrementWorkerCount()
                return null
            }
            val wc = workerCountOf(c)
            if (shouldExitThisWorker(timedOut, wc)) {
                if (compareAndDecrementWorkerCount(c)) {
                    return null
                }
                continue
            }
            try {
                waitWorkerCount.incrementAndGet()
                val task = if (allowCoreThreadTimeOut || wc > corePoolSizeValue) {
                    workQueue.poll(keepAliveTimeNanos, TimeUnit.NANOSECONDS)
                } else {
                    workQueue.take()
                }
                waitWorkerCount.decrementAndGet()
                if (task != null) {
                    return task
                }
                timedOut = true
            } catch (_: InterruptedException) {
                waitWorkerCount.decrementAndGet()
                timedOut = false
            }
        }
    }

    private fun shouldExitWorker(rs: Int): Boolean {
        return rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())
    }

    private fun shouldExitThisWorker(timedOut: Boolean, wc: Int): Boolean {
        val maximumExceeded = wc > maximumPoolSizeValue
        val poolTimeOut = (allowCoreThreadTimeOut || wc > corePoolSizeValue) && timedOut
        return (maximumExceeded || poolTimeOut) && (wc > 1 || workQueue.isEmpty())
    }

    private fun interruptWorker(thread: Thread): Boolean {
        return (runStateAtLeast(ctl.get(), STOP) || (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) &&
            !thread.isInterrupted
    }

    private fun reject(command: Command) {
        command.commandPool.onReject(command)
    }

    private fun isWorkerEmpty(): Boolean {
        return !hasWorker(ctl.get())
    }

    private fun canCreateCoreThread(c: Int): Boolean {
        return workerCountOf(c) < corePoolSizeValue
    }

    private fun hasWorker(workerCount: Int): Boolean {
        return workerCountOf(workerCount) != 0
    }

    private fun hasIdleWorker(): Boolean {
        return waitWorkerCount.toInt() > 0
    }

    fun waitWorkerCount(): Int {
        return waitWorkerCount.get()
    }

    private fun executeCreate(command: Command) {
        var c = ctl.get()
        if (workerCountOf(c) < maximumPoolSizeValue) {
            if (addWorker(command, false)) {
                return
            }
            c = ctl.get()
        }
        if (isRunning(c) && workQueue.offer(command)) {
            val recheck = ctl.get()
            if (!isRunning(recheck) && remove(command)) {
                reject(command)
            } else if (!hasWorker(recheck)) {
                addWorker(null, false)
            }
        } else {
            reject(command)
        }
    }

    private fun noNeedAdd(firstTask: Command?, rs: Int): Boolean {
        return rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workQueue.isEmpty())
    }

    private fun advanceRunState(targetState: Int) {
        while (true) {
            val c = ctl.get()
            if (runStateAtLeast(c, targetState) || ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c)))) {
                break
            }
        }
    }

    private fun checkShutdownAccess() {
        val security = System.getSecurityManager()
        if (security != null) {
            security.checkPermission(shutdownPerm)
            mainLock.lock()
            try {
                for (worker in workers) {
                    security.checkAccess(worker.thread)
                }
            } finally {
                mainLock.unlock()
            }
        }
    }

    private fun interruptWorkers() {
        mainLock.lock()
        try {
            for (worker in workers) {
                worker.interruptIfStarted()
            }
        } finally {
            mainLock.unlock()
        }
    }

    protected open fun onShutdown() {
    }

    fun isRunningOrShutdown(shutdownOK: Boolean): Boolean {
        val rs = runStateOf(ctl.get())
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK)
    }

    private fun drainQueue(): List<Command> {
        val taskList = ArrayList<Command>()
        workQueue.drainTo(taskList)
        if (!workQueue.isEmpty()) {
            for (task in workQueue.toTypedArray()) {
                if (workQueue.remove(task)) {
                    taskList.add(task)
                }
            }
        }
        return taskList
    }
}
