package com.onus.demotest.threadpool

import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

open class CVFutureTask<V> : FutureTask<V> {
    @JvmField
    var task: Any

    constructor(callable: Callable<V>) : super(callable) {
        task = callable
    }

    constructor(runnable: Runnable, result: V) : super(runnable, result) {
        task = runnable
    }
}
