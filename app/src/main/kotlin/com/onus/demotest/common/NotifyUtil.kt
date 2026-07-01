package com.onus.demotest.common

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.lang.reflect.InvocationTargetException

/**
 * @Author: onuszhao
 * @Date: 2024-01-23 21:10
 * @Description:
 */
object NotifyUtil {

    /**
     * 获取通知开关开启状态
     * true-开启 false-关闭
     */
    fun isNotifyEnabled(context: Context): Boolean {
        return checkNotifyStatus(context) == NotifyEnableStatus.ENABLE
    }

    /**
     * 获取通知开关开启状态，根据 NotificationManagerCompat#areNotificationsEnabled 源码改写
     *
     * @return 参考 [NotifyEnableStatus]
     */
    @SuppressLint("ObsoleteSdkInt")
    fun checkNotifyStatus(context: Context): Int {
        val CHECK_OP_NO_THROW = "checkOpNoThrow"
        val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 以下是 NotificationManagerCompat 中的源码
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (mNotificationManager.areNotificationsEnabled()) NotifyEnableStatus.ENABLE else NotifyEnableStatus.DISABLE
            } catch (e: Exception) {
                NotifyEnableStatus.CHECK_FAIL
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val appInfo = context.applicationInfo
            val pkg = if (context.applicationContext == null) context.packageName else context.applicationContext.packageName
            val uid = appInfo.uid
            try {
                val appOpsClass = Class.forName(AppOpsManager::class.java.name)
                val checkOpNoThrowMethod = appOpsClass.getMethod(
                    CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String::class.java
                )
                val opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
                val value = opPostNotificationValue[Int::class.java] as Int
                if (checkOpNoThrowMethod.invoke(appOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED) 1 else 0
            } catch (e: ClassNotFoundException) {
                NotifyEnableStatus.CHECK_FAIL
            } catch (e: NoSuchMethodException) {
                NotifyEnableStatus.CHECK_FAIL
            } catch (e: NoSuchFieldException) {
                NotifyEnableStatus.CHECK_FAIL
            } catch (e: InvocationTargetException) {
                NotifyEnableStatus.CHECK_FAIL
            } catch (e: IllegalAccessException) {
                NotifyEnableStatus.CHECK_FAIL
            } catch (e: RuntimeException) {
                NotifyEnableStatus.CHECK_FAIL
            }
        } else {
            NotifyEnableStatus.ENABLE
        }
    }

    /**
     * 打开通知设置
     */
    fun openNotifySetting(context: Context) {
        var localIntent: Intent? = Intent()
        localIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            localIntent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            localIntent.putExtra("app_package", context.packageName)
            localIntent.putExtra("app_uid", context.applicationInfo.uid)
        } else {
            localIntent = null
        }
        if (localIntent == null) {
            openSystemSetting(context)
        } else {
            context.startActivity(localIntent)
        }
    }

    /**
     * 检测通知渠道状态
     *
     * @return 0-通知渠道关闭 1-通知渠道开启 2-Android版本小于API26 3-检测失败 4-通道未创建
     */
    fun checkChannelStatus(context: Context, channelId: String?): Int {
        var ret = 3
        if (isEqualORAboveApi26()) {
            try {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (nm != null) {
                    ret = if (nm.getNotificationChannel(channelId) != null) {
                        val enable = nm.getNotificationChannel(channelId).importance != NotificationManager.IMPORTANCE_NONE
                        if (enable) 1 else 0
                    } else {
                        4
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ret = 2
        }
        return ret
    }

    /**
     * 判断是否大于等于API26
     */
    fun isEqualORAboveApi26(): Boolean {
        return isEqualORAboveTargetApi(Build.VERSION_CODES.O)
    }

    /**
     * 判断是否大于等于指定API
     */
    fun isEqualORAboveTargetApi(targetApi: Int): Boolean {
        return Build.VERSION.SDK_INT >= targetApi
    }

    /**
     * 打开系统设置
     */
    private fun openSystemSetting(context: Context) {
        try {
            val localIntent = Intent()
            localIntent.action = Settings.ACTION_SETTINGS
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(localIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知开关状态
     */
    interface NotifyEnableStatus {
        companion object {
            /**
             * 开启
             */
            const val ENABLE = 1

            /**
             * 关闭
             */
            const val DISABLE = 0

            /**
             * 检测失败
             */
            const val CHECK_FAIL = 2
        }
    }
}