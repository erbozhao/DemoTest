package com.onus.demotest.core.threadpool.lib

interface IWorkerListener {
    fun onWorkerAdded(workerId: String)

    fun onWorkerExited(workerId: String)
}
