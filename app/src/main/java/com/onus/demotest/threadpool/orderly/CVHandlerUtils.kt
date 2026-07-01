package com.onus.demotest.threadpool.orderly

import com.onus.demotest.threadpool.CVExecutorSupplier
import java.util.concurrent.Executor

/**
 * Cloud View 2022 copyright.
 *
 * User: Jiantao.Yang
 * Date: 2022/10/2
 * Time: 4:28 下午
 */
object CVHandlerUtils {

    private const val TAG = "ThreadMonitor"

    internal var msgMonitor: ICVMsgMonitor = ICVMsgMonitor.DEFAULT_INSTANCE

    /**
     * 任务消息执行过程监控
     */
    fun setMsgMonitor(monitor: ICVMsgMonitor?) {
        if (monitor != null) {
            msgMonitor = monitor
        }
    }

    /**
     * 返回对应类型的CVHandler实例
     */
    fun getExecutor(type: CVHandlerType): Executor {
        return when (type) {
            CVHandlerType.MAIN_THREAD -> {
                CVExecutorSupplier.forMainThreadTasks()
            }
            CVHandlerType.BACKGROUND_THREAD -> {
                CVExecutorSupplier.forBackgroundTasks()
            }
            CVHandlerType.DB_THREAD -> {
                CVExecutorSupplier.forDbTasks()
            }
            CVHandlerType.IO_THREAD -> {
                CVExecutorSupplier.forIoTasks()
            }
            CVHandlerType.SHORT_TIME_THREAD -> {
                CVExecutorSupplier.forShortTimeTasks()
            }
            CVHandlerType.LONG_TIME_THREAD -> {
                CVExecutorSupplier.forLongTimeTasks()
            }
        }
    }

    /**
     * 不同类型Handler消息执行超时监控时间
     */
    fun getExeTimeout(type: CVHandlerType): Long {
        return msgMonitor.cvHandlerTimeout(type)
    }

    fun report(type: CVHandlerType, performanceData: MsgPerformanceData) {
        msgMonitor.report(type, performanceData)
    }
}

enum class CVHandlerType {
    MAIN_THREAD,
    SHORT_TIME_THREAD,
    LONG_TIME_THREAD,
    BACKGROUND_THREAD,
    IO_THREAD,
    DB_THREAD
}