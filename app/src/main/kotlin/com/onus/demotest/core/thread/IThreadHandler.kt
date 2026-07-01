package com.onus.demotest.core.thread

/**
 * @Author: onuszhao
 * @Date: 2024-04-16 10:34
 * @Description:
 */
interface IThreadHandler {
    fun post(runnable: Runnable)
}