package com.onus.demotest.threadpool.lib

interface CommandRejectedExecutionHandler {
    fun rejectedExecution(r: Runnable, commandPool: CommandPool)
}
