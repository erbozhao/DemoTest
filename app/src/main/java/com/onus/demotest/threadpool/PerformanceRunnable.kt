package com.onus.demotest.threadpool

import java.lang.StringBuilder

/**
 * Cloud View 2022 copyright.
 *
 * Description:
 * User: Jiantao.Yang
 * Date: 2022/12/9
 * Time: 9:49 下午
 */
class PerformanceRunnable(val runnable: Runnable) : Runnable {

    // 用于获取runnable 堆栈
    private val throwable = Throwable()

    override fun run() {
        runnable.run()
    }

    fun getCallStack(): String {
        val stackTrace = throwable.stackTrace
        val strBuilder = StringBuilder()
        stackTrace.forEach {
            if (it.className.contains("com.cloudview.core.threadpool")) {
                return@forEach
            }

            if (it.className.startsWith("java.") || it.className.startsWith("android.")) {
                return@forEach
            }

            strBuilder.append("at ")
            strBuilder.append(it.className)
            strBuilder.append(":")
            strBuilder.append(it.methodName)
            strBuilder.append("(" + it.lineNumber + ")")
            strBuilder.append("\n")
        }
        return strBuilder.toString()
    }
}