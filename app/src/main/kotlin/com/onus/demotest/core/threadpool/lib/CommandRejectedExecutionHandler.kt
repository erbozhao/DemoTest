package com.onus.demotest.core.threadpool.lib

interface CommandRejectedExecutionHandler {
    fun rejectedExecution(r: Runnable, commandPool: CommandPool)
}
