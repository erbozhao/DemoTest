package com.onus.demotest.thread

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/**
 * @Author: onuszhao
 * @Date: 2024-04-16 10:33
 * @Description:
 */
interface IThreadHandlerFactory {
    fun create(): IThreadHandler

    companion object {

        private class SimpleThreadFactory : ThreadFactory {
            override fun newThread(r: Runnable?): Thread {
                return Thread(r, "TP_PHX_EVENT_EMITTER")
            }
        }

        private val DEFAULT = object : IThreadHandlerFactory {
            override fun create(): IThreadHandler {
                return getDefaultThreadHandler()
            }
        }

        private fun getDefaultThreadHandler(): IThreadHandler {
            return object : IThreadHandler {
                val executor = Executors.newSingleThreadExecutor(SimpleThreadFactory())
                override fun post(runnable: Runnable) {
                    executor.execute(runnable)
                }
            }
        }

        fun getDefaultFactory() = DEFAULT
    }
}