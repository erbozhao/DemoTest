package com.onus.demotest.core.threadpool.lib

interface RunnableListener {
    fun beforeExecute(runnable: Runnable)

    fun afterExecute(runnable: Runnable)
}
