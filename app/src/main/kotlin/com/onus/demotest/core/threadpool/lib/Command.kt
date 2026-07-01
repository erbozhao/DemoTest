package com.onus.demotest.core.threadpool.lib

open class Command(
    @JvmField var runnable: Runnable,
    @JvmField var commandPool: ICommandPool,
    @JvmField var priority: Int
) {
    @JvmField
    var performanceData: CommandPerformanceData = CommandPerformanceData()

    fun recordQueueTime(value: Long) {
        performanceData.queueTime = value
    }

    fun recordStartExeTime(value: Long) {
        performanceData.startExeTime = value
    }

    fun recordCompletedTime(value: Long) {
        performanceData.completeTime = value
    }
}
