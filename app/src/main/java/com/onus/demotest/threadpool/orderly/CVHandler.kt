package com.onus.demotest.threadpool.orderly

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import java.util.concurrent.Executor
import java.util.concurrent.PriorityBlockingQueue

/**
 * Cloud View 2022 copyright.
 *
 * Description: 提供类似系统Handler能力（添加及执行消息任务）和监控。
 * 使用场景：需要按序执行的后台任务、定时任务等
 * User: Jiantao.Yang
 * Date: 2022/10/1
 * Time: 4:57 下午
 *
 */
open class CVHandler @JvmOverloads constructor(
    internal val type: CVHandlerType,
    val mCallback: Callback? = null
) {

    private val mWakeupHandler by lazy {
        Handler(wakeupHandlerThread.looper)
    }

    private val mWakeupRunnable by lazy {
        Runnable { wakeup() }
    }

    @Synchronized
    private fun wakeup() {
        if (isWaiting) {
            tryExecuteMessage()
        }
    }

    private val messageQueue = PriorityBlockingQueue(11, CVMessage.COMPARATOR)

    /**
     * 消息队列是否处于等待状态: 队列为空 或者 队头消息未到执行时间
     */
    private var isWaiting = true

    /**
     * 正在执行的消息线程
     */
    var currentExeThread: Thread? = null
        private set

    /**
     * 用于执行消息的线程
     */
    private val msgExecutor: Executor = CVHandlerUtils.getExecutor(type)

    fun post(runnable: Runnable) {
        postDelayed(runnable, 0)
    }

    fun postDelayed(runnable: Runnable, delayMillis: Long) {
        val msg = obtainMessage()
        msg.callback = runnable
        sendMessageDelayed(msg, delayMillis)
    }

    /**
     * Sends a Message containing only the what value.
     */
    fun sendEmptyMessage(what: Int): Boolean {
        return sendEmptyMessageDelayed(what, 0)
    }

    /**
     * Sends a Message containing only the what value, to be delivered
     * after the specified amount of time elapses.
     */
    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        val msg = obtainMessage()
        msg.what = what
        return sendMessageDelayed(msg, delayMillis)
    }

    /**
     * Subclasses must implement this to receive messages.
     */
    open fun handleMessage(msg: CVMessage) {
    }

    fun dispatchMessage(msg: CVMessage) {
        if (msg.callback != null) {
            msg.callback?.run()
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return
                }
            }
            handleMessage(msg)
        }
    }

    /**
     * get a message
     */
    fun obtainMessage() = CVMessage.obtain(this)

    @JvmOverloads
    fun obtainMessage(what: Int, obj: Any? = null) = CVMessage.obtain(this, what, obj)

    @Synchronized
    @JvmOverloads
    fun hasMessages(what: Int, obj: Any? = null): Boolean {
        val iterator = messageQueue.iterator()
        while (iterator.hasNext()) {
            val msg = iterator.next()
            if (msg.what == what && (obj == null || obj == msg.obj)) {
                return true
            }
        }
        return false
    }

    @Synchronized
    @JvmOverloads
    fun removeMessages(what: Int, obj: Any? = null) {
        val iterator = messageQueue.iterator()
        while (iterator.hasNext()) {
            val msg = iterator.next()
            if (msg.what == what && (obj == null || obj == msg.obj)) {
                iterator.remove()
            }
        }
    }

    @Synchronized
    @JvmOverloads
    fun removeCallbacks(runnable: Runnable, obj: Any? = null) {
        val iterator = messageQueue.iterator()
        while (iterator.hasNext()) {
            val msg = iterator.next()
            if (msg.callback == runnable && (obj == null || obj == msg.obj)) {
                iterator.remove()
            }
        }
    }

    /**
     * Remove any pending posts of callbacks and sent messages
     */
    @Synchronized
    fun removeCallbacksAndMessages() {
        mWakeupHandler.removeCallbacksAndMessages(null)
        messageQueue.clear()
    }

    fun sendMessage(msg: CVMessage): Boolean {
        return sendMessageDelayed(msg, 0)
    }

    fun sendMessageDelayed(msg: CVMessage, delayMillis: Long): Boolean {
        val uptimeMillis = if (delayMillis <= 0) {
            SystemClock.uptimeMillis()
        } else SystemClock.uptimeMillis() + delayMillis
        return sendMessageAtTime(msg, uptimeMillis)
    }

    private fun sendMessageAtTime(msg: CVMessage, uptimeMillis: Long): Boolean {
        enqueueMessage(msg, uptimeMillis)
        return true // 默认消息入队成功
    }

    @Synchronized
    private fun enqueueMessage(msg: CVMessage, `when`: Long) {
        msg.`when` = `when`
        val head = messageQueue.peek()
        // New head, wake up the event queue
        val needWake = (head == null || `when` == 0L || `when` < head.`when`)
        // add to queue
        msg.onEnqueue()
        messageQueue.offer(msg)

        // LogUtils.d(TAG, "enqueueMessage $msg")

        if (needWake && isWaiting) {
            tryExecuteMessage()
        }
    }

    /**
     * 尝试执行队列中的任务
     * 调用时机：新消息入队、上一个消息执行完毕、定时唤醒触发
     */
    @Synchronized
    private fun tryExecuteMessage() {
        // LogUtils.d(TAG, "tryExecuteMessage  >>> msgSize = ${messageQueue.size}")
        // Try to retrieve the next message.  Return if found.
        val nextMsg = messageQueue.peek() ?: return
        val now = SystemClock.uptimeMillis()
        val offset = nextMsg.`when` - now
        if (offset <= 0) {
            // Got a message.
            isWaiting = false
            doExecuteMessage(messageQueue.poll())
        } else {
            // Next message is not ready.  Set a timeout to wake up when it is ready.
            isWaiting = true
            mWakeupHandler.removeCallbacks(mWakeupRunnable)
            mWakeupHandler.postDelayed(mWakeupRunnable, offset) // reset wakeup offset time
            // LogUtils.d(TAG, "tryExecuteMessage  mWakeupRunnable delayTime = $offset")
        }
    }

    @Synchronized
    private fun doBeforeExecute() {
        currentExeThread = Thread.currentThread()
    }

    @Synchronized
    private fun doAfterExecute() {
        if (messageQueue.size > 0) {
            tryExecuteMessage() // 队列里还有消息
        } else {
            isWaiting = true
        }
        currentExeThread = null
    }

    private fun doExecuteMessage(msg: CVMessage?) {
        msgExecutor.execute {
            // LogUtils.d(TAG, "doExecuteMessage >>> $msg")
            doBeforeExecute()
            msg?.let {
                it.onStartExe()
                dispatchMessage(it)
                it.onFinishExe()
            }
            // LogUtils.d(TAG, "doExecuteMessage <<< size = ${messageQueue.size}")
            doAfterExecute()
        }
    }

    /**
     * Callback interface you can use when instantiating a Handler to avoid
     * having to implement your own subclass of Handler.
     */
    interface Callback {
        /**
         * @return True if no further handling is desired
         */
        fun handleMessage(msg: CVMessage): Boolean
    }

    companion object {
        private const val TAG = "CVHandler"

        /**
         * 消息唤醒线程，全局共用
         */
        private val wakeupHandlerThread: HandlerThread by lazy {
            HandlerThread("ThreadPool_threadHandler_wakeup").apply {
                start()
            }
        }
    }
}