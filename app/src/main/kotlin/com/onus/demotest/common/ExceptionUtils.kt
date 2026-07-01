package com.onus.demotest.common

/**
 * @Author: onuszhao
 * @Date: 2023-10-12 15:51
 * @Description:
 */
object ExceptionUtils {
    fun getExceptionInfo(e: Exception): List<String> {
        val exceptionInfo = mutableListOf<String>()
        kotlin.runCatching {
            e.message?.let {
                exceptionInfo.add(it)
            }
            e.stackTrace?.forEach {
                exceptionInfo.add(it.toString())
            }
            e.cause?.message?.let {
                exceptionInfo.add(it)
            }
        }
        return exceptionInfo
    }
}