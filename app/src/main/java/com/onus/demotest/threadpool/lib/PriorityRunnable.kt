package com.onus.demotest.threadpool.lib

abstract class PriorityRunnable : Runnable, Comparable<PriorityRunnable> {
    private var priority: Long = 0

    fun getPriority(): Long {
        return priority
    }

    fun setPriority(priority: Long) {
        this.priority = priority
    }

    override fun compareTo(other: PriorityRunnable): Int {
        val delta = priority - other.priority
        return when {
            delta > 0 -> -1
            delta < 0 -> 1
            else -> 0
        }
    }
}
