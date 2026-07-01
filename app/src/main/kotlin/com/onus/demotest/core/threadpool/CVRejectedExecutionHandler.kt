package com.onus.demotest.core.threadpool

interface CVRejectedExecutionHandler {
    fun rejectedExecution(r: Runnable, executor: CVThreadPoolExecutor)
}
