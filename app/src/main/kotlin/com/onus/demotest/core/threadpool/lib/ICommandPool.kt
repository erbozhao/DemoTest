package com.onus.demotest.core.threadpool.lib

interface ICommandPool : Comparable<ICommandPool> {
    fun takeWaitingCommand(): Command?

    fun onCmdExecuted(command: Command)

    fun getMaximumThreadSize(): Int

    fun onReject(command: Command)

    fun getName(): String?

    fun beforeExecute(command: Command)

    fun afterExecute(command: Command)

    fun isTerminated(): Boolean
}
