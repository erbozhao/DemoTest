package com.onus.demotest.threadpool.lib

interface ICommandListener {
    fun onCmdStart(commandPool: CommandPool, command: Command)

    fun onCmdCompleted(commandPool: CommandPool, command: Command)

    fun onQueueMaxSizeChanged(commandPool: CommandPool, maxQueueSize: Int)
}
