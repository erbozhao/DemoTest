package com.onus.demotest.utils.compat

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * @Author: onuszhao
 * @Date: 2023-12-29 11:41
 * @Description:
 */
object PackageManagerCompat {
    @JvmStatic
    @Throws(PackageManager.NameNotFoundException::class, UnsupportedOperationException::class)
    fun getPackageInfo(packageManager: PackageManager?, packageName: String, flag: Int): PackageInfo? {
        packageManager ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flag.toLong()))
        } else {
            packageManager.getPackageInfo(packageName, flag)
        }
    }

    @JvmStatic
    @Throws(PackageManager.NameNotFoundException::class, UnsupportedOperationException::class)
    fun getApplicationInfo(packageManager: PackageManager?, packageName: String, flag: Int): ApplicationInfo? {
        packageManager ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(flag.toLong()))
        } else {
            packageManager.getApplicationInfo(packageName, flag)
        }
    }
}