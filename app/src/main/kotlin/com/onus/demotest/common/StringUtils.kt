package com.onus.demotest.common

import java.util.Locale

/**
 * @Author: onuszhao
 * @Date: 2024-01-18 19:34
 * @Description:
 */
object StringUtils {
    private const val K = 1024f
    private const val M = (1024 * 1024).toFloat()
    private const val G = (1024 * 1024 * 1024).toFloat()
    private const val SIZE_UNIT_BYTE = "B"
    private const val SIZE_UNIT_KB = "KB"
    private const val SIZE_UNIT_MB = "MB"
    private const val SIZE_UNIT_GB = "GB"

    fun getSizeString(size: Float, digit: Int, needSpace: Boolean = false): String? {
        if (size < 0) {
            return null
        }
        val locale = Locale.ENGLISH
        var split = " "
        var numberFormat = "%d"
        if (digit > 0) {
            numberFormat = "%." + digit + "f"
        }
        if (!needSpace) {
            split = ""
        }
        return if (size < K) {
            String.format(locale, "%d$split$SIZE_UNIT_BYTE", size.toInt())
        } else if (size < M) {
            String.format(locale, numberFormat + split + SIZE_UNIT_KB, size / K)
        } else if (size < G) {
            String.format(locale, numberFormat + split + SIZE_UNIT_MB, size / M)
        } else {
            String.format(locale, numberFormat + split + SIZE_UNIT_GB, size / G)
        }
    }
}