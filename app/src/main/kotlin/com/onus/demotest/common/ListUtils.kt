package com.onus.demotest.common

import java.io.File
import java.util.Collections

/**
 * @Author: onuszhao
 * @Date: 2024-01-18 20:12
 * @Description:
 */
object ListUtils {
    fun sortByTime(result: List<File>) {
        Collections.sort(result) { old, new ->
            val oldInt = old.lastModified()
            val newInt = new.lastModified()
            when {
                oldInt > newInt -> {
                    -1
                }
                oldInt < newInt -> {
                    1
                }
                else -> 0
            }
        }
    }
}