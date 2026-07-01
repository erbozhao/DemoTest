package com.onus.demotest.core.threadpool

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService

interface CVExecutorService : ExecutorService {
    fun remove(command: Runnable): Boolean

    fun getQueue(): BlockingQueue<Runnable>
}
