package com.onus.demotest.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.onus.demotest.R
import com.onus.demotest.feature.notification.MediaPlaybackServiceCompat.Companion.CHANNEL_ID
import com.onus.demotest.common.NotificationUtils

/**
 * @author onuszhao
 * @since 2025/12/8
 * @description
 */

class CallNotificationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 关键步骤：在启动服务时，将其提升为前台服务
        showNotifyCall()

        // 3. 处理服务停止逻辑（例如，当通话结束时，可以调用 stopSelf()）
        return START_NOT_STICKY
    }

    private fun showNotifyCall() {
        val CHANNEL_ID = "Channel_Call"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "通话样式通知"
            val descriptionText = "通话样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val NOTIFICATION_ID = NotifyDemoActivity.Companion.generateNotifyId()
        // 2. 准备 PendingIntent 和图标
        // 假设 CallActivity 是点击通知内容后要跳转的 Activity
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // 接听操作
        val answerIntent = Intent(this, IncomingCallActivity::class.java).setAction(Intent.ACTION_ANSWER)
        val answerPendingIntent = PendingIntent.getBroadcast(
            this, 1, answerIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 拒接操作
        val declineIntent = Intent(this, IncomingCallActivity::class.java).setAction("ACTION_DECLINE")
        val declinePendingIntent = PendingIntent.getBroadcast(
            this, 2, declineIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // --- 4. 构建通知 ---
        val caller = Person.Builder()
            .setName("Jane DoeJane DoeJane DoeJane DoeJane DoeJane DoeJane DoeJane DoeJane Doe")
            .setImportant(true)
            .build()

        val callStyle = NotificationCompat.CallStyle.forIncomingCall(caller, declinePendingIntent, answerPendingIntent)
        callStyle.setAnswerButtonColorHint(Color.GREEN)
        callStyle.setDeclineButtonColorHint(Color.RED)
        callStyle.setIsVideo(true)
        callStyle.setVerificationText("张三来电张三来电张三来电张三来电张三来电张三来电张三来电张三来电张三来电")
        callStyle.setVerificationIcon(BitmapFactory.decodeResource(this.resources, R.drawable.message_icon))
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话")
            .setContentText("来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            // 关键：设置 Full-Screen Intent
            .setFullScreenIntent(fullScreenPendingIntent, true) // 第二个参数 true 表示它是全屏的
            .setStyle(callStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())
        builder.setGroup("Group_Call")

        startForeground(NOTIFICATION_ID,  builder.build())
    }

}