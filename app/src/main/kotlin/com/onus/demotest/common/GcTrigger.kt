package com.onus.demotest.common

/**
 * @Author: onuszhao
 * @Date: 2023-09-28 14:53
 * @Description:
 */
object GcTrigger {

    fun runGc() {
        // Code taken from AOSP FinalizationTest:
        // https://android.googlesource.com/platform/libcore/+/master/support/src/test/java/libcore/
        // java/lang/ref/FinalizationTester.java
        // System.gc() does not garbage collect every time. Runtime.gc() is
        // more likely to perform a gc.
        Runtime.getRuntime().gc()
        enqueueReferences()
        System.runFinalization()
    }

    private fun enqueueReferences() {
        // Hack. We don't have a programmatic way to wait for the reference queue daemon to move
        // references to the appropriate queues.
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            throw AssertionError()
        }
    }
}