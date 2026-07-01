package com.onus.demotest.threadpool.orderly

import android.os.SystemClock

/**
 * Cloud View 2022 copyright.
 *
 * Description:
 * User: Jiantao.Yang
 * Date: 2022/10/1
 * Time: 5:23 下午
 */
class CVMessage private constructor(private val target: CVHandler) {
    private val type: CVHandlerType = target.type
    @JvmField
    var what: Int = 0

    @JvmField
    var arg1: Int = 0

    @JvmField
    var arg2: Int = 0

    @JvmField
    var obj: Any? = null

    internal var callback: Runnable? = null

    /**
     * The targeted delivery time of this message. The time-base is SystemClock.uptimeMillis.
     */
    internal var `when`: Long = 0

    /**
     * 消息入队时间。如果执行时间相同（毫秒级别），则先入队先执行。
     */
    private var enqueueNanos: Long = 0

    /**
     * 性能监控数据
     */
    private val performanceData by lazy { MsgPerformanceData() }

    companion object {
        private const val TAG = "CVMessage"
        internal fun obtain(target: CVHandler) = CVMessage(target)

        internal fun obtain(target: CVHandler, what: Int, obj: Any?) = CVMessage(target).apply {
            this.what = what
            this.obj = obj
        }

        internal val COMPARATOR = object : Comparator<CVMessage> {
            /**
             * when 值越小先执行，越靠前
             */
            override fun compare(o1: CVMessage?, o2: CVMessage?): Int {
                if (o1 == null || o2 == null || o1 == o2) {
                    return 0
                }
                return if (o1.`when` == o2.`when`) {
                    o1.enqueueNanos.compareTo(o2.enqueueNanos)
                } else o1.`when`.compareTo(o2.`when`)
            }
        }
    }

    fun sendToTarget() {
        target.sendMessage(this)
    }

    /**
     * 消息进入队列
     */
    fun onEnqueue() {
        enqueueNanos = System.nanoTime()
        if (CVHandlerUtils.msgMonitor.enable()) {
            performanceData.planExeTime = `when`
            performanceData.queueTime = SystemClock.uptimeMillis()
        }
    }

    /**
     * 开始执行
     */
    fun onStartExe() {
        if (CVHandlerUtils.msgMonitor.enable()) {
            performanceData.startExeTime = SystemClock.uptimeMillis()
        }
    }

    /**
     * 执行结束
     */
    fun onFinishExe() {
        if (CVHandlerUtils.msgMonitor.enable()) {
            performanceData.finishExeTime = SystemClock.uptimeMillis()
            if (performanceData.getExeTimeCost() >= CVHandlerUtils.getExeTimeout(type)) {
                CVHandlerUtils.report(type, performanceData)
            }
        }
    }

    override fun toString(): String {
        return "CVMessage(what=$what, arg1=$arg1, arg2=$arg2, obj=$obj, `when`=$`when`)"
    }
}