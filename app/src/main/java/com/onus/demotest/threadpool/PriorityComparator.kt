package com.onus.demotest.threadpool

import java.util.Comparator

open class PriorityComparator : Comparator<Any> {
    override fun compare(o1: Any?, o2: Any?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }

        var o1Obj: Any = o1
        if (o1 is CVFutureTask<*>) {
            o1Obj = o1.task
        }

        var o2Obj: Any = o2 ?: return 0
        if (o2 is CVFutureTask<*>) {
            o2Obj = o2.task
        }

        val leftObj = if (o1Obj is PriorityComparable) o1Obj else null
        val rightObj = if (o2Obj is PriorityComparable) o2Obj else null
        val leftPriority = leftObj?.priority() ?: PriorityComparable.Priority.NORMAL
        val rightPriority = rightObj?.priority() ?: PriorityComparable.Priority.NORMAL

        return if (leftPriority == rightPriority) {
            val leftSequence = leftObj?.sequence() ?: 0
            val rightSequence = rightObj?.sequence() ?: 0
            leftSequence - rightSequence
        } else {
            rightPriority.ordinal - leftPriority.ordinal
        }
    }

    companion object {
        @JvmField
        val DEFAULT = PriorityComparator()
    }
}
