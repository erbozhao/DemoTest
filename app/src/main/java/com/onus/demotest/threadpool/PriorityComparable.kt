package com.onus.demotest.threadpool

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
