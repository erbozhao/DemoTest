package com.onus.demotest.feature.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.service.PushMonitorService
import com.onus.demotest.common.DeviceUtils
import com.onus.demotest.common.NotifyUtil

/**
 * @Author: onuszhao
 * @Date: 2023-10-12 14:33
 * @Description:
 */
class BindNotificationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var container: LinearLayout
    private lateinit var openMonitor: Button
    private lateinit var closeMonitor: Button
    private lateinit var showNotify: Button
    private lateinit var updateChannel: Button
    private lateinit var showNotifyDefault: Button
    private lateinit var showNotifyMax: Button
    private lateinit var showNotifyMaxColorized: Button
    private lateinit var showNotifyMaxMessage: Button
    private lateinit var resultContainer: LinearLayout

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEdit: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@BindNotificationActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        openMonitor = Button(this).apply {
            isAllCaps = false
            text = "Open Monitor"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(com.onus.demotest.R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        closeMonitor = Button(this).apply {
            isAllCaps = false
            text = "Close Monitor"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotify = Button(this).apply {
            isAllCaps = false
            text = "Show Notify"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        updateChannel = Button(this).apply {
            isAllCaps = false
            text = "Update Channel"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper2, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyDefault = Button(this).apply {
            isAllCaps = false
            text = "Default Notify"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper2.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyMax = Button(this).apply {
            isAllCaps = false
            text = "Max Notify"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper2.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyMaxColorized = Button(this).apply {
            isAllCaps = false
            text = "Max Colorized Notify"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper2.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyMaxMessage = Button(this).apply {
            isAllCaps = false
            text = "Max Message Notify"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@BindNotificationActivity)
            wrapper2.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val scrollView = ScrollView(this)
        container.addView(scrollView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            weight = 1f
        })

        resultContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            scrollView.addView(this)
        }

        sharedPreferences = getSharedPreferences("demo_test_notify_preferences", MODE_PRIVATE)
        sharedPreferencesEdit = sharedPreferences.edit()
    }

    override fun onClick(v: View?) {
        v ?: return
        clearResult()
        when (v) {
            openMonitor -> {
                openMonitor()
            }
            closeMonitor -> {
                closeNotificationListenerService(applicationContext)
            }
            showNotify -> {
                showNotification()
            }
            updateChannel -> {
                updateChannel()
            }
            showNotifyDefault -> {
                showNotifyDefault(showNotifyDefault)
            }
            showNotifyMax -> {
               showNotifyMax(showNotifyMax)
            }
            showNotifyMaxColorized -> {
                showNotifyMaxColorized(showNotifyMaxColorized)
            }
            showNotifyMaxMessage -> {
                showNotifyMaxMessage(showNotifyMaxMessage)
            }
        }
    }

    private fun openMonitor() {
        // 判断是否开启监听通知权限
        if (NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(packageName)) {
            Toast.makeText(applicationContext, "通知权限获取成功", Toast.LENGTH_SHORT).show()
            closeNotificationListenerService(applicationContext)
            Handler(Looper.getMainLooper()).postDelayed({
                openNotificationListenerService(applicationContext)
            }, 500) // 等待500ms后再开启
        } else {
            gotoNotificationAccessSetting(applicationContext)
        }
    }

    private fun gotoNotificationAccessSetting(context: Context): Boolean {
        kotlin.runCatching {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(intent, KEY_REQUEST_CODE)
            return true
        }.onFailure {
            // 普通情况下找不到的时候需要再特殊处理找一次
            kotlin.runCatching {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$NotificationAccessSettingsActivity")
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                startActivityForResult(intent, KEY_REQUEST_CODE)
                return true
            }
        }
        Toast.makeText(context, "对不起，您的手机暂不支持", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun closeNotificationListenerService(context: Context) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, PushMonitorService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun openNotificationListenerService(context: Context) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, PushMonitorService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY_REQUEST_CODE) {
            if (NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(packageName)) {
                Toast.makeText(applicationContext, "通知权限获取成功", Toast.LENGTH_SHORT).show()
                closeNotificationListenerService(applicationContext)
                openNotificationListenerService(applicationContext)
            } else {
                Toast.makeText(applicationContext, "通知权限获取失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification() {
        val curVersion = sharedPreferences.getLong("test_channel_version", 1)
        val CHANNEL_ID = "notify_test_channel_$curVersion"
        val CHANNEL_NAME = "Notify Test Channel"
        val CHANNEL_DESCRIPTION = "My Channel Description"

        // val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        val builder = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Notification.Builder(applicationContext) else Notification.Builder(
            applicationContext,
            CHANNEL_ID
        )
        // builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("test title $curVersion")
        builder.setContentText("test message")
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
        // builder.setShowWhen(true)
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

        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道（仅适用于 Android 8.0 及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // notificationManager.notificationChannels?.forEach { channel ->
            //     val channelState = NotifyUtil.checkChannelStatus(applicationContext, channel.id)
            //     if (channelState == 0) {
            //         notificationManager.deleteNotificationChannel(channel.id)
            //     }
            //     Log.d("onuszhao", "before  channelID=${channel.id}  channelState=${NotifyUtil.checkChannelStatus(applicationContext, channel.id)}")
            // }

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.importance = NotificationManager.IMPORTANCE_MAX
            channel.enableVibration(true)
            channel.enableLights(false)
            channel.setShowBadge(false)
            // channel.lockscreenVisibility = Notification.VISIBILITY_SECRET  //需要注意的是：该方法是无法生效的，只能被系统或者排序排序服务所更改。

            // channel.description = CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(channel)

            notificationManager.notificationChannels?.forEach { channel ->
                Log.d("onuszhao", "after  channelID=${channel.id}  channelState=${NotifyUtil.checkChannelStatus(applicationContext, channel.id)}")
            }
        }


        val notification = builder.build().apply {
            this.flags = this.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_FOREGROUND_SERVICE
        }

        // 显示通知
        notificationManager.notify(926, notification)

        // 延迟重刷渠道
        // Handler(Looper.getMainLooper()).postDelayed({
        //     notificationManager.notify(926, notification)
        // }, 5000)

        // 延迟重刷渠道
        // Handler(Looper.getMainLooper()).postDelayed({
        //     (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.let {
        //         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
        //             val newChannelId = "notify_test_channel_${curVersion + 1}"
        //
        //             arrayOf(CHANNEL_ID, newChannelId).forEach { channelId ->
        //                 val channelState = NotifyUtil.checkChannelStatus(applicationContext, channelId)
        //                 Log.d("onuszhao", "before  channelID=${channelId}  channelState=$channelState   allChannel=${it.notificationChannels.map { it.id }}")
        //             }
        //
        //             kotlin.runCatching {
        //                 it.deleteNotificationChannel(CHANNEL_ID)
        //
        //                 val newChannel = NotificationChannel(newChannelId, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        //                 newChannel.importance = NotificationManager.IMPORTANCE_HIGH
        //                 // channel.description = CHANNEL_DESCRIPTION
        //                 notificationManager.createNotificationChannel(newChannel)
        //             }
        //
        //
        //             arrayOf(CHANNEL_ID, newChannelId).forEach { channelId ->
        //                 var result = 3
        //                 kotlin.runCatching {
        //                     val notifyChannel = it.getNotificationChannel(channelId)
        //                     if (notifyChannel != null) {
        //                         val enable = notifyChannel.importance != NotificationManager.IMPORTANCE_NONE
        //                         result =  if (enable) 1 else 0
        //                     } else {
        //                         result = 4
        //                     }
        //                 }
        //                 Log.d("onuszhao", "after  channelID=${channelId}  result=$result  allChannel=${it.notificationChannels.map { it.id }}")
        //             }
        //         }
        //     }
        // }, 5000)
    }

    private fun updateChannel() {
        val oldVersion = sharedPreferences.getLong("test_channel_version", 1)
        sharedPreferencesEdit.putLong("test_channel_version", oldVersion + 1)
        sharedPreferencesEdit.apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotifyDefault(button: Button){
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val text = button.text.toString()

        val channel = NotificationChannel("SMART_PANEL", "SMART_PANEL", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "destrc"
        channel.lockscreenVisibility = 1
        // channel.enableVibration(true)

        manager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, "SMART_PANEL")
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setShowWhen(true)
        // builder.setOngoing(true)
        // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.setColorized(true)
        builder.setColor(Color.BLUE)
        builder.setDefaults(4)
        // builder.setf
        builder.setGroup(text)
        // builder.setColor(Color.RED)
        builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        // builder.setSilent(true)

        builder.setPriority(NotificationCompat.PRIORITY_MAX)

        builder.setContentText(text)
        builder.setTicker("sjsksd")
        manager.notify(10001, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotifyMax(button: Button){
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val text = button.text.toString()

        val channel = NotificationChannel(text, "SMART_PANEL", NotificationManager.IMPORTANCE_MAX)
        channel.description = "destrc"
        channel.lightColor = Color.RED
        channel.lockscreenVisibility = 1
        channel.enableVibration(true)

        manager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, text)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setShowWhen(true)
        // builder.setOngoing(true)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        // builder.setColorized(true)
        // builder.setColor(Color.BLUE)
        // builder.setDefaults(4)
        // builder.setf
        // builder.setColor(Color.RED)
        // builder.setGroupAlertBehavior()
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        builder.setGroup("phx")
        builder.setGroupSummary(false)
        builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        // builder.setGroup()
        // builder.setGroupSummary()
        builder.setSilent(true)

        // val bitmapDrawable = resources.getDrawable(R.drawable.ic_launcher_background) as BitmapDrawable
        // builder.setLargeIcon(bitmapDrawable.bitmap)
        // val remoteView = RemoteViews(packageName, R.layout.notification_layout)
        // builder.setContentTitle("hello ")
        // builder.setCustomContentView(remoteView)
        // builder.setCustomBigContentView(remoteView)
        // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        // builder.setPriority(PRIORITY_MAX)

        //forgroundservice + 染色才能生效
        builder.setContentText(text)
        // builder.setTicker("sjsksd")

        manager.notify(10002, builder.build().apply {
            // this.flags = this.flags or 0x00000800
            // this.flags = this.flags or Notification.FLAG_FOREGROUND_SERVICE

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotifyMaxColorized(button: Button){
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val text = button.text.toString()
        val sortKey = "sort_key_001"

        val channel = NotificationChannel(text, "News Bar", NotificationManager.IMPORTANCE_MAX)
        channel.description = "destrc"
        channel.lightColor = Color.RED
        channel.lockscreenVisibility = 1
        channel.enableVibration(true)
        manager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, text)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        // builder.setShowWhen(true)
        builder.setOngoing(true)
        // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.setColorized(true)
        // builder.setDefaults(4)
        builder.setAutoCancel(false)
        builder.setGroup(text)
        builder.setColor(Color.RED)
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        // builder.setSilent(true)
        // builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        builder.setSortKey(sortKey)

        // val bitmapDrawable = resources.getDrawable(R.drawable.ic_launcher_background) as BitmapDrawable
        // builder.setLargeIcon(     bitmapDrawable.bitmap)
        val remoteView = RemoteViews(packageName, R.layout.notification_colorized)
        builder.setContentTitle("hello ")
        builder.setCustomContentView(remoteView)
        builder.setContent(remoteView)
        builder.setCustomBigContentView(remoteView)
        // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        // builder.setPriority(PRIORITY_MAX)
        builder.setStyle(NotificationCompat.MessagingStyle(sortKey))
        builder.setContentText(text)
        // builder.setTicker("sjsksd")

        manager.notify(10003, builder.build().apply {
            // this.flags = this.flags or 0x00000800
            this.flags = this.flags or Notification.FLAG_FOREGROUND_SERVICE or Notification.FLAG_NO_CLEAR
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotifyMaxMessage(button: Button){
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val text = button.text.toString()
        val sortKey = "sort_key_002"

        val channel = NotificationChannel(text, "Push", NotificationManager.IMPORTANCE_MAX)
        channel.description = "destrc"
        channel.lightColor = Color.RED
        channel.lockscreenVisibility = 1
        channel.enableVibration(true)

        manager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, text)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        // builder.setShowWhen(true)
        // builder.setOngoing(true)
        // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        // builder.setColorized(true)
        // builder.setColor(Color.BLUE)
        builder.setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        builder.setGroup(text)
        builder.setAutoCancel(true)
        // builder.setColor(Color.RED)
        // builder.setGroupAlertBehavior()
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        // builder.setGroupSummary(true)
        // builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        // builder.setSilent(true)
        builder.setSortKey(sortKey)

        val foldRemoteView = RemoteViews(packageName, R.layout.notification_message_fold)
        // val expandRemoteView = RemoteViews(packageName, R.layout.notification_message_expand)
        builder.setContentTitle("hello ")
        builder.setCustomContentView(foldRemoteView)  //折叠
        builder.setCustomBigContentView(foldRemoteView)  // 展开
        builder.setCustomHeadsUpContentView(foldRemoteView)
        // builder.setContent(foldRemoteView)
        // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        // val EXTRA_MEDIA_SESSION = "android.mediaSession"
        //
        // builder.extras.putParcelable(EXTRA_MEDIA_SESSION,object :Parcelable{
        //     override fun describeContents(): Int {
        //         return  0
        //     }
        //
        //     override fun writeToParcel(dest: Parcel, flags: Int) {
        //     }
        //
        //
        // })

        // builder.setStyle(style)
        builder.setStyle(NotificationCompat.MessagingStyle(sortKey))
        builder.setContentText(text)
        // builder.setTicker("sjsksd")

        manager.notify(10004, builder.build().apply {
            // this.flags = this.flags or 0x00000800
            // this.flags = this.flags or Notification.FLAG_FOREGROUND_SERVICE
        })

        // applicationContext.startForegroundService()
    }

    fun createResultLine(str: String) {
        val textLine = TextView(this).apply {
            text = str
            textSize = resources.getDimension(R.dimen.dp_6)
            setTextColor(resources.getColor(R.color.black))
        }
        resultContainer.addView(
            textLine,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun clearResult() {
        resultContainer.removeAllViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun release() {
        val intent = Intent(this, PushMonitorService::class.java)
        stopService(intent)
    }

    companion object {
        val KEY_REQUEST_CODE = View.generateViewId()
    }
}