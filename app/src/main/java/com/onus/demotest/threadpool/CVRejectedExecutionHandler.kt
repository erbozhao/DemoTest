package com.onus.demotest.threadpool

interface CVRejectedExecutionHandler {
    fun rejectedExecution(r: Runnable, executor: CVThreadPoolExecutor)
}
