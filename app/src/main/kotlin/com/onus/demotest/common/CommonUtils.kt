package com.onus.demotest.common

import android.content.Context
import android.text.format.DateUtils
import com.onus.demotest.common.compat.PackageManagerCompat
import java.text.SimpleDateFormat

/**
 * @Author: onuszhao
 * @Date: 2023-10-12 19:46
 * @Description:
 */
object CommonUtils {

    // @JvmStatic
    var hasBoot = false

    fun format(time: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(time)
    }

    fun millisToMinutes(millis: Long, digit: Int = 2): Double {
        val minutes = millis / (DateUtils.MINUTE_IN_MILLIS * 1.0)
        return "%.${digit}f".format(minutes).toDouble()
    }

    fun bytesToMegabytes(bytes: Long, digit: Int = 2): Double {
        val megabytes = bytes.toDouble() / (1000 * 1000)
        return "%.${digit}f".format(megabytes).toDouble()
    }

    fun bytesToGigabytes(bytes: Long, digit: Int = 2): Double {
        val gigabytes = bytes.toDouble() / (1024 * 1024 * 1024)
        return "%.${digit}f".format(gigabytes).toDouble() // 保留两位小数
    }

    fun isPkgInstalled(packageName: String, context: Context): Boolean {
        var isExist = true
        try {
            val pm = context.packageManager
            PackageManagerCompat.getPackageInfo(pm, packageName, 0)
        } catch (e: Exception) {
            isExist = false
        }
        return isExist
    }
}