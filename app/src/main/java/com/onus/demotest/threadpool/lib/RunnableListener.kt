package com.onus.demotest.threadpool.lib

interface RunnableListener {
    fun beforeExecute(runnable: Runnable)

    fun afterExecute(runnable: Runnable)
}
