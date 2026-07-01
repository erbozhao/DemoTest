package com.onus.demotest.core.threadpool.orderly

/**
 * Cloud View 2022 copyright.
 *
 * Description:
 * User: Jiantao.Yang
 * Date: 2022/10/1
 * Time: 9:38 下午
 */
class MsgPerformanceData() {
    /**
     * 计划执行时间
     */
    internal var planExeTime: Long = 0

    /**
     * 入队时间
     */
    internal var queueTime: Long = 0
        set(value) {
            field = value
            if (queueStack == null) {
                queueStack = getStack(Thread.currentThread().stackTrace, "", -1)
            }
        }

    /**
     * 入队时业务调用堆栈
     */
    internal var queueStack: String? = null

    /**
     * 开始执行时间
     */
    internal var startExeTime: Long = 0

    /**
     * 执行结束时间
     */
    internal var finishExeTime: Long = 0

    /**
     * 任务堵塞耗时,单位ms
     */
    fun getBlockTimeCost(): Long {
        return startExeTime - planExeTime
    }

    /**
     * 任务执行耗时,单位ms
     */
    fun getExeTimeCost(): Long {
        return finishExeTime - startExeTime
    }

    /**
     * 任务完成总耗时，单位ms
     */
    fun getTotalTimeCost(): Long {
        return finishExeTime - planExeTime
    }

    fun getStack(
        trace: Array<StackTraceElement>?,
        preFixStr: String,
        limitSet: Int
    ): String {
        var limit = limitSet
        if (trace == null || trace.size < 3) {
            return ""
        }
        if (limit < 0) {
            limit = Int.MAX_VALUE
        }
        val result = StringBuilder(" \n")
        var i = 3
        while (i < trace.size - 3 && i < limit) {
            with(result) {
                append(preFixStr)
                append("at ")
                append(trace[i].className)
                append(":")
                append(trace[i].methodName)
                append("(" + trace[i].lineNumber + ")")
                append("\n")
            }
            i++
        }
        return result.toString()
    }

    override fun toString(): String {
        return "blockExeTime=${getBlockTimeCost()}, exeTimeCost=${getExeTimeCost()}, totalTimeCost=${getTotalTimeCost()}\n stack: \n$queueStack"
    }
}