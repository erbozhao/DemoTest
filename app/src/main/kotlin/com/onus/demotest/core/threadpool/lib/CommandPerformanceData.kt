package com.onus.demotest.core.threadpool.lib

open class CommandPerformanceData {
    @JvmField
    var startExeTime: Long = 0

    @JvmField
    var queueTime: Long = 0

    @JvmField
    var completeTime: Long = 0

    @JvmField
    var maxRunningCount: Int = 0

    fun getQueueTimeCost(): Long {
        return startExeTime - queueTime
    }

    fun getExeTimeCost(): Long {
        return completeTime - startExeTime
    }

    fun getTotalTimeCost(): Long {
        return completeTime - queueTime
    }
}
