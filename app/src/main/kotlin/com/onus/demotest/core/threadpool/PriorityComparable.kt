package com.onus.demotest.core.threadpool

interface PriorityComparable {
    enum class Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    fun priority(): Priority

    fun sequence(): Int
}
