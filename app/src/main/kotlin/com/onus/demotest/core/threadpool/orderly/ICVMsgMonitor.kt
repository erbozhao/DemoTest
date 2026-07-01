package com.onus.demotest.core.threadpool.orderly

import android.util.Log

/**
 * Cloud View 2022 copyright.
 *
 * Description:
 * User: Jiantao.Yang
 * Date: 2022/10/21
 * Time: 11:58 上午
 */
interface ICVMsgMonitor {

    companion object {
        // 此TAG关联测试平台日志收集
        private const val TAG = "ThreadMonitor"
        internal val DEFAULT_INSTANCE = object : ICVMsgMonitor {}
    }

    /**
     * 是否开启监控
     */
    fun enable(): Boolean = false

    /**
     * 不同CVHandler类型超时时间
     */
    fun cvHandlerTimeout(type: CVHandlerType): Long {
        return when (type) {
            CVHandlerType.MAIN_THREAD -> 1000
            CVHandlerType.BACKGROUND_THREAD -> 8000
            CVHandlerType.DB_THREAD -> 10000
            CVHandlerType.IO_THREAD -> 10000
            CVHandlerType.SHORT_TIME_THREAD -> 2000
            CVHandlerType.LONG_TIME_THREAD -> 5000
        }
    }

    /**
     * 监控数据上报
     */
    fun report(type: CVHandlerType, performanceData: MsgPerformanceData) {
        Log.d(TAG, "HandlerType:$type , performanceData:$performanceData")
    }
}