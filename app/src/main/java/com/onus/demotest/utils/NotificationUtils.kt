package com.onus.demotest.utils

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.onus.demotest.R
import com.onus.demotest.pages.notification.BindNotificationActivity.Companion.KEY_REQUEST_CODE
import com.onus.demotest.pages.notification.NotificationBroadcastReceiver
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author onuszhao
 * @since 2025/12/2
 * @description
 */

object NotificationUtils {

    /**
     * 排序(NotificationComparator.compare):
     * 1.Importance: notify.flag(0 != (n.flags & Notification.FLAG_HIGH_PRIORITY) + notify.priority + channel.importance + (mSound != null || mVibration != null) + (n.fullScreenIntent != null)
     * 2.RankingScore: 设置sortKey, 与Adjustment中包含key
     * 3.ImportantColorized: (record.getImportance() >= NotificationManager.IMPORTANCE_LOW) -> extras.getBoolean("android.colorized") && (flags & Notification.FLAG_CAN_COLORIZE != 0 ||  flags & Notification.FLAG_FOREGROUND_SERVICE != 0)
     *                          13及以上: (notification.isForegroundService() && ServiceNotificationPolicy == NOT_FOREGROUND_SERVICE)则notification.flags &= ~FLAG_FOREGROUND_SERVICE
     * 4.ImportantOngoing: (record.getImportance() >= NotificationManager.IMPORTANCE_LOW) -> CallStyle + (flags & Notification.FLAG_FOREGROUND_SERVICE != 0)
     *                          + (record.isCategory(Notification.CATEGORY_CALL) && isDefaultPhoneApp(record.getSbn().getPackageName())) + (MediaStyle +  extras.getParcelable("android.mediaSession", MediaSession.Token.class) != null)
     * 5.ImportantMessaging: (importance < NotificationManager.IMPORTANCE_LOW) -> (MessagingStyle || (CATEGORY_MESSAGE && isDefaultMessagingApp(sbn))
     * 6.ImportantPeople: (record.getImportance() >= NotificationManager.IMPORTANCE_LOW) -> (record.getContactAffinity() > ValidateNotificationPeople.NONE)
     * 7.SystemMax: (record.getImportance() >= NotificationManager.IMPORTANCE_HIGH) -> ("android".equals(packageName)) || ("com.android.systemui".equals(packageName))
     * 8.PackagePriority
     * 9.Notification.priority
     * 10.Interruptive: postNotification() ->  r.setInterruptive(isInterruptive) -> isVisuallyInterruptive() -> areStyledNotificationsVisiblyDifferent()
     * 11.RankingTimeMs: (n.when != 0 && n.when <= getSbn().getPostTime())则n.when -> previousRankingTimeMs>0则previousRankingTimeMs -> getPostTime
     * 二次排序(RankingHelper.sort):
     * 1.记录一次排序及过，并计算group排序
     * 2.getCriticality -> (record.isRecentlyIntrusive() && record.getImportance() > NotificationManager.IMPORTANCE_MIN) -> groupProxy.getAuthoritativeRank(分组排序) -> (mGroupKey != null && (flags & FLAG_GROUP_SUMMARY) != 0) -> record.getAuthoritativeRank(记录排序)
     *
     *  5.0、6.0:     packagePriority>score>联系人亲密度（ValidateNotificationPeople）>发送时间	packagePriority:系统设置的字段，类似充电这些
     *                      scroe:对应最老版本设置的 prority字段(min default high max)
     *                      联系人亲密度：系统定义的如果是通讯录好友的消息
     * 7.0:         importance>packagePriority>priority>联系人亲密度>发送时间	importance：系统添加的一个层级，有声音或者震动的大于静默通知
     * 8.0-9.0:    Colorized>onging>Message类型>ImportPeople（系统计算）> 联系人亲密度>packagePriority>priority>时间	Colorized:必须要是前台通知栏，且设置通知栏染色
     *                      ongoing:音乐播放器类型或者是打电话类型的通知
     *                      message类型：短信类型的通知
     * 10:          渠道优先级>Colorized>onging>Message类型>ImportPeople（系统计算）> 联系人亲密度>packagePriority>priority>时间	渠道优先级：min default high max
     * 11-14:       渠道优先级>系统打分>Colorized>onging>Message类型>ImportPeople（系统计算）> 联系人亲密度>packagePriority>priority>interrupteve>发送时间	系统打分:自定义通知管理器的时候NotificationAssistantService可以自定义分数interrupteve:系统用来用做是否曝光过
     */
    fun showNotification(context: Context, channelId: String, channelName: String) {
        // val builder = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) NotificationCompat.Builder(context) else NotificationCompat.Builder(context, channelId)
        val builder = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Notification.Builder(context) else Notification.Builder(context, channelId)
        builder.setWhen(System.currentTimeMillis())  // 设置通知时间
        builder.setShowWhen(true)  // 是否显示时间
        builder.setSmallIcon(R.mipmap.ic_launcher)  // 小图标：必须提供
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.music_album))  // 大图标：通常仅用于联系人头像，请勿将其用于应用图标。
        builder.setContentTitle("这个是测试通知的标题。")
        builder.setContentText("这个是测试通知的内容。")
        // 通知操作
        val confirmIntent = getPendingIntent(context, "CONFIRM")
        val cancelIntent = getPendingIntent(context, "CANCEL")
        val acceptAction = Notification.Action.Builder(R.drawable.music_play, "Confirm", confirmIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            acceptAction.setAuthenticationRequired(true) // true-锁屏界面时需要求验证身份，false-锁屏界面时由系统决定是否验证身份
            acceptAction.setAllowGeneratedReplies(true)  //允许生成回复，且有RemoteInput才生效
        }
        val rejectAction = Notification.Action.Builder(R.drawable.music_pause, "Cancel", cancelIntent)
        builder.addAction(acceptAction.build())
        builder.addAction(rejectAction.build())

        // builder.setTicker("test")
        // builder.setVibrate()
        // builder.setLights()
        // builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setPriority(Notification.PRIORITY_MAX)
        builder.setGroup("ranker_group")
        // builder.setGroupSummary(true)
        builder.setOngoing(true)
        builder.setSortKey("ranker_group")
        // builder.setDefaults(NotificationCompat.DEFAULT_ALL)  // 默认配置,包括通知的提示音,震动效果等
        // builder.setCategory(Notification.CATEGORY_MESSAGE)
        // builder.setVibrate()
        // builder.setExtras()
        // builder.setBubbleMetadata()
        // builder.setCustomContentView(remoteViews)    //折叠后通知显示的布局
        // builder.setCustomHeadsUpContentView(remoteViews)//横幅样式显示的布局
        // builder.setCustomBigContentView(remoteViews) //展开后通知显示的布局
        // builder.addAction(R.mipmap.ic_avatar, "去看看", pendingIntent)// 通知上的操作
        // builder.setContent(remoteViews)              //兼容低版本
        // builder.setContentIntent(pendingIntent)     //一定要设置，点击整个remoteView就可跳转
        // builder.setCategory(NotificationCompat.CATEGORY_SYSTEM) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
        // builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
        builder.setVisibility(Notification.VISIBILITY_PUBLIC) // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
        builder.setAutoCancel(false)                   // 允许点击后清除通知
        // builder.setOnlyAlertOnce(true)
        // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // builder.setStyle(Notification.MessagingStyle("test"))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setFlag(Notification.FLAG_NO_CLEAR, true)
            builder.setFlag(Notification.FLAG_FOREGROUND_SERVICE, true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // builder.setColor(resources.getColor(R.color.white))//小图标的颜色
            builder.setColorized(true)
            builder.setColor(Color.RED)
            // builder.setLights()
            // builder.setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)  //Notification.GROUP_ALERT_SUMMARY不弹headsup, 否则不设置
        }

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // 创建通知渠道（仅适用于 Android 8.0 及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // notificationManager.notificationChannels?.forEach { channel ->
            //     val channelState = NotifyUtil.checkChannelStatus(applicationContext, channel.id)
            //     if (channelState == 0) {
            //         notificationManager.deleteNotificationChannel(channel.id)
            //     }
            //     Log.d("onuszhao", "before  channelID=${channel.id}  channelState=${NotifyUtil.checkChannelStatus(applicationContext, channel.id)}")
            // }

            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.importance = NotificationManager.IMPORTANCE_MAX
            channel.enableVibration(true)
            channel.enableLights(false)
            channel.setShowBadge(false)
            // channel.lockscreenVisibility = Notification.VISIBILITY_SECRET  //需要注意的是：该方法是无法生效的，只能被系统或者排序排序服务所更改。

            // channel.description = CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(channel)

            notificationManager.notificationChannels?.forEach { channel ->
                Log.d("onuszhao", "after  channelID=${channel.id}  channelState=${NotifyUtil.checkChannelStatus(context, channel.id)}")
            }
        }
        val notification = builder.build().apply {
            this.flags = this.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_FOREGROUND_SERVICE
        }

        // 显示通知
        notificationManager.notify(926, notification)
    }

    /**
     * 1. 跳转到应用的系统通知设置主界面。
     * 适用于所有支持通知设置的 Android 版本 (API 21+)。
     */
    fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                // 推荐：Android 8.0 (Oreo) 及更高版本
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                // 兼容：Android 5.0 到 7.1 (Lollipop 到 Nougat)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra("app_package", context.packageName) // 旧的 extra key
                    putExtra("app_uid", context.applicationInfo.uid)
                }
                // 回退方案：跳转到应用详情页（用户需要再点一次通知设置）
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
            addCategory(Intent.CATEGORY_DEFAULT)
            // 使用 FLAG_ACTIVITY_NEW_TASK 允许在非 Activity 环境下启动
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开通知设置界面", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * 2. (API 26+) 直接跳转到特定通知渠道的设置界面。
     * @param channelId 目标通知渠道的 ID。
     */
    fun openNotificationChannelSettings(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // 如果特定渠道跳转失败（理论上不应该），则回退到应用通知主界面
                Toast.makeText(context, "无法打开特定渠道设置，尝试跳转到应用通知主界面。", Toast.LENGTH_LONG).show()
                openAppNotificationSettings(context)
            }
        } else {
            // API 26 以下没有渠道概念，直接跳转到应用主设置页
            Toast.makeText(context, "当前系统版本不支持直接跳转到通知渠道设置。", Toast.LENGTH_SHORT).show()
            openAppNotificationSettings(context)
        }
    }

    fun gotoNotificationAccessSetting(context: Activity): Boolean {
        kotlin.runCatching {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivityForResult(intent, KEY_REQUEST_CODE)
            return true
        }.onFailure {
            // 普通情况下找不到的时候需要再特殊处理找一次
            kotlin.runCatching {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$NotificationAccessSettingsActivity")
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                context.startActivityForResult(intent, KEY_REQUEST_CODE)
                return true
            }
        }
        Toast.makeText(context, "对不起，您的手机暂不支持", Toast.LENGTH_SHORT).show()
        return false
    }

    // 辅助函数 (简化 PendingIntent 获取)
    fun getPendingIntent(context: Context, action: String, mutable: Boolean = false): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            this.action = action
        }
        val flags = if (mutable) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}