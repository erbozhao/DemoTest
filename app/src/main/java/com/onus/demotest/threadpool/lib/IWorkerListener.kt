package com.onus.demotest.threadpool.lib

interface IWorkerListener {
    fun onWorkerAdded(workerId: String)

    fun onWorkerExited(workerId: String)
}
