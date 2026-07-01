package com.onus.demotest.feature.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.session.MediaSessionCompat
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.graphics.drawable.IconCompat
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.common.DeviceUtils
import com.onus.demotest.common.NotificationUtils

/**
 * @author onuszhao
 * @since 2025/12/2
 * @description
 */

class NotifyDemoActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var scrollView: ScrollView
    private lateinit var container: LinearLayout

    private lateinit var requestPermission: Button
    private lateinit var goNotifySetting: Button
    private lateinit var goChannelSetting: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEdit: SharedPreferences.Editor

    private lateinit var showNotifyDefault: Button
    private lateinit var showNotifyDefault1: Button
    private lateinit var showNotifyDefault2: Button
    private lateinit var showNotifyBigText: Button
    private lateinit var showNotifyBigText2: Button
    private lateinit var showNotifyBigPic: Button
    private lateinit var showNotifyBigPic2: Button
    private lateinit var showNotifyInbox: Button
    private lateinit var showNotifyInbox2: Button
    private lateinit var showNotifyProgress: Button
    private lateinit var showNotifyProgress2: Button
    private lateinit var showNotifyMedia: Button
    private lateinit var showNotifyMedia2: Button
    private lateinit var showNotifyMedia3: Button
    private lateinit var showNotifyMessage: Button
    private lateinit var showNotifyCall: Button
    private lateinit var showNotifyCall2: Button
    private lateinit var showNotifyFull: Button
    private lateinit var showNotifyCustom: Button
    private lateinit var showNotifyGroup: Button
    private lateinit var showNotifyTest: Button
    private lateinit var showNotifyPush: Button
    private lateinit var showNotifyPush2: Button
    private lateinit var showNotifyPush3: Button
    private lateinit var showNotifyClean: Button
    private lateinit var showNotifyClean2: Button
    private lateinit var showNotifyClean3: Button
    private lateinit var showNotifyClean4: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        scrollView = ScrollView(this).apply {
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@NotifyDemoActivity), 0, 0)
        }
        setContentView(scrollView)
        ActivityStateManager.setCurActivity(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTheme(R.style.ActivityMain_Light)
        } else {
            setTheme(R.style.ActivityMain)
        }

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            scrollView.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        }

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        requestPermission = Button(this).apply {
            isAllCaps = false
            text = "请求权限"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        goNotifySetting = Button(this).apply {
            isAllCaps = false
            text = "跳转通知设置"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        goChannelSetting = Button(this).apply {
            isAllCaps = false
            text = "跳转通知渠道设置"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        sharedPreferences = getSharedPreferences("post_notify_preferences", MODE_PRIVATE)
        sharedPreferencesEdit = sharedPreferences.edit()

        val wrapper1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper1, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyDefault = Button(this).apply {
            isAllCaps = false
            text = "基础样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper1.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyDefault1 = Button(this).apply {
            isAllCaps = false
            text = "基础样式\n(仅title)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper1.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyDefault2 = Button(this).apply {
            isAllCaps = false
            text = "基础样式\n(仅content)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper1.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper8 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper8, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyBigText = Button(this).apply {
            isAllCaps = false
            text = "大文本样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper8.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyBigText2 = Button(this).apply {
            isAllCaps = false
            text = "大文本样式\n(带icon)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper8.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyBigPic = Button(this).apply {
            isAllCaps = false
            text = "大图样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper8.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyBigPic2 = Button(this).apply {
            isAllCaps = false
            text = "大图样式\n(无大图)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper8.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper2, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyInbox = Button(this).apply {
            isAllCaps = false
            text = "收件箱样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper2.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyInbox2 = Button(this).apply {
            isAllCaps = false
            text = "收件箱样式\n(带icon)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper2.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyMessage = Button(this).apply {
            isAllCaps = false
            text = "消息样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper2.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper9 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper9, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyCall = Button(this).apply {
            isAllCaps = false
            text = "通话样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper9.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyCall2 = Button(this).apply {
            isAllCaps = false
            text = "通话样式\n(自定义)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper9.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper3, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyProgress = Button(this).apply {
            isAllCaps = false
            text = "进度条样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper3.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyProgress2 = Button(this).apply {
            isAllCaps = false
            text = "进度条样式\n(仅进度)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper3.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper4 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper4, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyMedia = Button(this).apply {
            isAllCaps = false
            text = "媒体样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper4.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyMedia2 = Button(this).apply {
            isAllCaps = false
            text = "媒体样式\n(自定义)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper4.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyMedia3 = Button(this).apply {
            isAllCaps = false
            text = "媒体样式\n(自定义带session)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper4.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper7 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper7, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyFull = Button(this).apply {
            isAllCaps = false
            text = "全屏样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper7.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper5 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper5, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyCustom = Button(this).apply {
            isAllCaps = false
            text = "自定义样式"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper5.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyGroup = Button(this).apply {
            isAllCaps = false
            text = "通知分组"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper5.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyTest = Button(this).apply {
            isAllCaps = false
            text = "测试通知"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper5.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper6 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper6, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyPush = Button(this).apply {
            isAllCaps = false
            text = "Push通知\n(大图)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper6.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyPush2 = Button(this).apply {
            isAllCaps = false
            text = "Push通知\n(消息)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper6.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyPush3 = Button(this).apply {
            isAllCaps = false
            text = "Push通知\n(消息无快捷)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper6.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val wrapper10 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper10, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        showNotifyClean = Button(this).apply {
            isAllCaps = false
            text = "Clean通知\n(基础)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper10.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyClean2 = Button(this).apply {
            isAllCaps = false
            text = "Clean通知\n(基础无图)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper10.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyClean3 = Button(this).apply {
            isAllCaps = false
            text = "Clean通知\n(消息)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper10.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        showNotifyClean4 = Button(this).apply {
            isAllCaps = false
            text = "Clean通知\n(消息无快捷)"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_4)
            setOnClickListener(this@NotifyDemoActivity)
            wrapper10.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v) {
            requestPermission -> {
                requestPermission()
            }

            goNotifySetting -> {
                NotificationUtils.openAppNotificationSettings(this)
            }

            goChannelSetting -> {
                NotificationUtils.openNotificationChannelSettings(this, "Channel_Default")
            }

            showNotifyDefault -> { // 基础样式
                showNotifyDefault()
            }

            showNotifyDefault1 -> { // 基础样式(仅title)
                showNotifyDefault(needContent = false)
            }

            showNotifyDefault2 -> { // 基础样式(仅content)
                showNotifyDefault(needTitle = false)
            }

            showNotifyBigText -> { // 大文本样式
                showNotifyBigText()
            }

            showNotifyBigText2 -> { // 大文本样式
                showNotifyBigText2()
            }

            showNotifyBigPic -> { // 大图样式
                showNotifyBigPic()
            }

            showNotifyBigPic2 -> { // 大图样式
                showNotifyBigPic2()
            }

            showNotifyInbox -> { // 收件箱样式
                showNotifyInbox()
            }

            showNotifyInbox2 -> { // 收件箱样式
                showNotifyInbox2()
            }

            showNotifyMessage -> { // 消息样式
                showNotifyMessage()
            }

            showNotifyCall -> { // 通话样式
                showNotifyCall()
            }

            showNotifyCall2 -> {
                showNotifyCall2()
            }

            showNotifyProgress -> { // 进度条样式
                showNotifyProgress()
            }

            showNotifyProgress2 -> { // 进度条样式2
                showNotifyProgress2()
            }

            showNotifyMedia -> { // 媒体样式
                showNotifyMedia()
            }

            showNotifyMedia2 -> { // 媒体样式
                showNotifyMedia2()
            }

            showNotifyMedia3 -> { // 媒体样式
                showNotifyMedia3()
            }

            showNotifyFull -> {  // 全屏样式
                showNotifyFull()
            }

            showNotifyCustom -> { // 装饰自定义样式
                showNotifyCustom()
            }

            showNotifyPush -> {
                showNotifyPush()
            }

            showNotifyPush2 -> {
                showNotifyPush2(true)
            }

            showNotifyPush3 -> {
                showNotifyPush2(false)
            }

            showNotifyClean -> {
                showNotifyClean(true)
            }

            showNotifyClean2 -> {
                showNotifyClean(false)
            }

            showNotifyClean3 -> {
                showNotifyClean2()
            }

            showNotifyClean4 -> {
                showNotifyClean3()
            }

            showNotifyGroup -> { // 通知分组
                showNotifyGroup()
            }

            showNotifyTest -> { // 测试通知
                NotificationUtils.showNotification(this, "channelId", "channelName")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("onuszhao", "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("onuszhao", "onStop")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d("onuszhao", "onWindowFocusChanged  hasFocus=$hasFocus")
    }

    private var backPressed = false
    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("onuszhao", "onBackPressed  backPressed=$backPressed")
        backPressed = true
    }

    private var mediaSession: MediaSessionCompat? = null
    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "MediaNotification")
        mediaSession?.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                // 处理播放逻辑
            }

            override fun onPause() {
                super.onPause()
                // 处理暂停逻辑
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                // 处理下一曲逻辑
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                // 处理上一曲逻辑
            }
        })

        // 激活 MediaSession
        mediaSession?.isActive = true
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        KeyEvent.ACTION_DOWN
        KeyEvent.KEYCODE_BACK
        Log.d("onuszhao", "dispatchKeyEvent  action=${event?.action}  ${event?.keyCode}")
        return super.dispatchKeyEvent(event)
    }

    /** SDK33时，请求运行时通知权限 */
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission =
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
            val requestNum = sharedPreferences.getLong("request_post_notify_num", 0)
            val hasDeniedPermission = sharedPreferences.getBoolean("request_post_notify_denied", false)
            if (!hasPermission) {
                if (shouldShow) {
                    // 拒绝过一次，再次请求
                    Log.d(
                        "onuszhao",
                        "requestPermission  仅拒绝过一次  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission"
                    )
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                    sharedPreferencesEdit.putBoolean("request_post_notify_denied", true)
                    sharedPreferencesEdit.apply()
                } else {
                    if (requestNum > 0) {
                        if (hasDeniedPermission) {
                            // 拒绝过多次次，请求也调不起来
                            Log.d(
                                "onuszhao",
                                "requestPermission  拒绝过多次  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission"
                            )
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                        } else {
                            // 未拒绝过，可以调起来
                            Log.d(
                                "onuszhao",
                                "requestPermission  未拒绝过  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission"
                            )
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                        }
                    } else {
                        // 首发请求
                        Log.d(
                            "onuszhao",
                            "requestPermission  首次  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission"
                        )
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                    }
                }
            } else {
                Log.d("onuszhao", "requestPermission 已授权")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(
            "onuszhao",
            "onRequestPermissionsResult  requestCode=$requestCode  permissions=${permissions.joinToString(",")}  grantResults=${
                grantResults.joinToString(",")
            }"
        )
        if (requestCode == REQUEST_CODE) {
            val hasPermission =
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
            val oldNum = sharedPreferences.getLong("request_post_notify_num", 0)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("onuszhao", "grant  success  oldNum=$oldNum  shouldShow=$shouldShow  hasPermission=$hasPermission")
            } else {
                Log.d("onuszhao", "grant  failed  oldNum=$oldNum   shouldShow=$shouldShow  hasPermission=$hasPermission")
            }

            // 记录弹出次数
            sharedPreferencesEdit.putLong("request_post_notify_num", oldNum + 1)
            sharedPreferencesEdit.apply()
        }
    }

    private fun showNotifyDefault(needTitle: Boolean = true, needContent: Boolean = true) {
        val CHANNEL_ID = "Channel_Default"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "默认样式通知"
            val descriptionText = "默认样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
        if (needTitle) {
            builder.setContentTitle("这是默认样式通知的标题，通常为一行，最多显示两行。这是默认样式通知的标题，通常为一行，最多显示两行。")
        }
        if (needContent) {
            val spanBuilder = SpannableString("这是默认样式通知的内容，通常为一行，最多显示两行。这是默认样式通知的内容，通常为一行，最多显示两行。")
            spanBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                5,
                10,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            builder.setContentText(spanBuilder)
        }
        builder.setSubText("这是默认样式通知的子文本，通常为一行，最多显示两行。这是默认样式通知的子文本，通常为一行，最多显示两行。")
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setGroup("Group_Default")
        // builder.setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.icon_stick))
        builder.addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())
        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyBigText() {
        val CHANNEL_ID = "Channel_Big_Text"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "大文本样式通知"
            val descriptionText = "大文本样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val longText =
            "这是一段非常长的文本内容示例。当通知展开时，用户可以看到所有这些文本，而不仅仅是标准通知中的一行摘要。它对于显示完整的消息或文章摘要非常有用。这是一段非常长的文本内容示例。当通知展开时，用户可以看到所有这些文本，而不仅仅是标准通知中的一行摘要。它对于显示完整的消息或文章摘要非常有用。这是一段非常长的文本内容示例。当通知展开时，用户可以看到所有这些文本，而不仅仅是标准通知中的一行摘要。它对于显示完整的消息或文章摘要非常有用。"
        val bidTextStyle = NotificationCompat.BigTextStyle()
        bidTextStyle.bigText(longText) // 设置展开后显示的全部文本
        bidTextStyle.setBigContentTitle("完整新闻标题完整新闻标题完整新闻标题完整新闻标题完整新闻标题完整新闻标题") // 可选：替换展开时的标题
        bidTextStyle.setSummaryText("来自应用XX来自应用XX来自应用XX来自应用XX") // 可选：设置摘要尾部文本
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("新闻摘要新闻摘要新闻摘要新闻摘要新闻摘要新闻摘要新闻摘要新闻摘要新闻摘要")
            .setContentText("点击展开查看全文点击展开查看全文点击展开查看全文点击展开查看全文点击展开查看全文") // 摘要文本
            .setStyle(bidTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())
        builder.setGroup("Group_Big_Text")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyBigText2() {
        val CHANNEL_ID = "Channel_Big_Text"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "大文本样式通知"
            val descriptionText = "大文本样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val largePicBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.largeicon)
        val longText =
            "Full-Time: 2 - 0\n" +
                "90' Goal: [2] - 0 Bruno Guimarães\n" +
                "2' Goal: [1] - 0 Sandro Tonali\n" +
                "Second Half Begins: 1 - 0\n" +
                "Half-Time: 1 - 0\n" +
                "35' Red Card: Chelsea's Nicolas Jackson"
        val bidTextStyle = NotificationCompat.BigTextStyle()
        // bidTextStyle.bigText(longText) // 设置展开后显示的全部文本
        // bidTextStyle.setBigContentTitle("完整新闻标题完整新闻标题完整新闻标题完整新闻标题完整新闻标题完整新闻标题") // 可选：替换展开时的标题
        // bidTextStyle.setSummaryText("来自应用XX来自应用XX来自应用XX来自应用XX") // 可选：设置摘要尾部文本
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setShowWhen(true)
            .setContentTitle("Breaking News")
            .setContentText(longText) // 摘要文本
            .setStyle(bidTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(largePicBitmap)
        builder.setGroup("Group_Big_Text")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyBigPic() {
        val CHANNEL_ID = "Channel_Big_Pic"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "大图样式通知"
            val descriptionText = "大图样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 假设您已经有一个 Bitmap 对象
        val largePicBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.img_beauty)
        val largeIconBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.icon_stick)
        val bigPicStyle = NotificationCompat.BigPictureStyle()
        bigPicStyle.bigPicture(largePicBitmap) // 设置展开后的大图
        // bigPicStyle.bigLargeIcon(largeIconBitmap) // 设置为 null 以隐藏在 Big Picture 模式下的左侧大图标
        bigPicStyle.setBigContentTitle("Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片")
        bigPicStyle.setSummaryText("查看更多照片和评论查看更多照片和评论查看更多照片和评论查看更多照片和评论查看更多照片和评论查看更多照片和评论查看更多照片和评论")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            bigPicStyle.setContentDescription("内容描述")
            bigPicStyle.showBigPictureWhenCollapsed(true)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传")
            .setContentText("Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！")
            .setStyle(bigPicStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(false)  // 允许每次触发都提醒
            .addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())
        builder.setGroup("Group_Big_Pic")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyBigPic2() {
        val CHANNEL_ID = "Channel_Big_Pic"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "大图样式通知"
            val descriptionText = "大图样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 假设您已经有一个 Bitmap 对象
        val largePicBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.largeicon)
        val bigPicStyle = NotificationCompat.BigPictureStyle()
        bigPicStyle.bigLargeIcon(largePicBitmap) // 设置为 null 以隐藏在 Big Picture 模式下的左侧大图标
        bigPicStyle.setBigContentTitle("Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片Sam的最新旅行照片")
        bigPicStyle.setSummaryText(
            "Full-Time: 2 - 0\n" +
                "90' Goal: [2] - 0 Bruno Guimarães\n" +
                "2' Goal: [1] - 0 Sandro Tonali\n" +
                "Second Half Begins: 1 - 0\n" +
                "Half-Time: 1 - 0\n" +
                "35' Red Card: Chelsea's Nicolas Jackson"
        )
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            bigPicStyle.setContentDescription("内容描述")
            // bigPicStyle.showBigPictureWhenCollapsed(false)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            // .setSmallIcon(IconCompat.createWithResource(this,R.drawable.largeicon)) // 必须设置的小图标
            .setContentTitle("新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传新照片上传")
            .setContentText("Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！Sam刚刚分享了一张新照片。点击查看！")
            .setStyle(bigPicStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.largeicon))
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        builder.setGroup("Group_Big_Pic2")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyInbox() {
        val CHANNEL_ID = "Channel_Inbox"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "收件箱样式通知"
            val descriptionText = "收件箱样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine("Jane: 会议定在下午两点。会议定在下午两点。会议定在下午两点。会议定在下午两点。会议定在下午两点。")
        inboxStyle.addLine("John: 附件已经上传了。 附件已经上传了。 附件已经上传了。 附件已经上传了。 附件已经上传了。")
        inboxStyle.addLine("Alex: 别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。")
        inboxStyle.addLine("System: 系统更新通知。系统更新通知。系统更新通知。系统更新通知。系统更新通知。系统更新通知。")
        inboxStyle.addLine("Jane: 会议定在下午两点。会议定在下午两点。会议定在下午两点。会议定在下午两点。会议定在下午两点。")
        inboxStyle.addLine("John: 附件已经上传了。 附件已经上传了。 附件已经上传了。 附件已经上传了。 附件已经上传了。")
        inboxStyle.addLine("Alex: 别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。")
        inboxStyle.addLine("System: 系统更新通知。系统更新通知。系统更新通知。系统更新通知。系统更新通知。系统更新通知。")
        inboxStyle.addLine("Jane: 会议定在下午两点。会议定在下午两点。会议定在下午两点。会议定在下午两点。会议定在下午两点。")
        inboxStyle.addLine("John: 附件已经上传了。 附件已经上传了。 附件已经上传了。 附件已经上传了。 附件已经上传了。")
        inboxStyle.addLine("Alex: 别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。别忘了周五的报告。")
        inboxStyle.addLine("System: 系统更新通知。系统更新通知。系统更新通知。系统更新通知。系统更新通知。系统更新通知。")
        inboxStyle.setBigContentTitle("4封未读邮件摘要4封未读邮件摘要4封未读邮件摘要4封未读邮件摘要4封未读邮件摘要4封未读邮件摘要")
        inboxStyle.setSummaryText("+12更多邮件+12更多邮件+12更多邮件+12更多邮件+12更多邮件+12更多邮件+12更多邮件")  //设置不显示
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件")
            .setContentText("来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 或者使用 setDefaults(Notification.DEFAULT_SOUND)
            .addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())
        builder.setGroup("Group_Inbox")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyInbox2() {
        val CHANNEL_ID = "Channel_Inbox"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "收件箱样式通知"
            val descriptionText = "收件箱样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine("Full-Time: 2 - 0")
        inboxStyle.addLine("90' Goal: [2] - 0 Bruno Guimarães")
        inboxStyle.addLine("2' Goal: [1] - 0 Sandro Tonali")
        inboxStyle.addLine("Second Half Begins: 1 - 0")
        inboxStyle.addLine("Half-Time: 1 - 0")
        inboxStyle.addLine("35' Red Card: Chelsea's Nicolas Jackson")
        inboxStyle.setSummaryText("+12更多邮件+12更多邮件+12更多邮件+12更多邮件+12更多邮件+12更多邮件+12更多邮件")  //设置不显示
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件你有4封新邮件")
            .setContentText("来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息来自Jane, John, 和 Alex消息")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 或者使用 setDefaults(Notification.DEFAULT_SOUND)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.largeicon))
        builder.setGroup("Group_Inbox")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyMessage() {
        val CHANNEL_ID = "Channel_Message"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "消息样式通知"
            val descriptionText = "消息样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC  // 设置锁屏可见
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 1. 定义用户
        val me = Person.Builder().setName("我").setIcon(IconCompat.createWithResource(this, R.drawable.icon_stick)).build()
        val sender = Person.Builder().setName("Alice").setIcon(IconCompat.createWithResource(this, R.drawable.music_album)).build()

        // 2. 创建 MessagingStyle
        val style = NotificationCompat.MessagingStyle(me)
        style.setConversationTitle("项目讨论组项目讨论组项目讨论组项目讨论组项目讨论组项目讨论组项目讨论组项目讨论组项目讨论组")
        style.setGroupConversation(true) // 这是一个群聊

        // 3. 添加消息
        style.addMessage(
            "好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。好的，我待会儿处理。",
            System.currentTimeMillis() - 5000,
            me
        )
        // style.addMessage("你收到我的文件了吗？你收到我的文件了吗？你收到我的文件了吗？你收到我的文件了吗？你收到我的文件了吗？你收到我的文件了吗？你收到我的文件了吗？你收到我的文件了吗？你收到我的文件了吗？", System.currentTimeMillis() - 2000, sender)
        style.addMessage("好的，我待会儿处理。", System.currentTimeMillis() - 5000, me)
        style.addMessage("你收到我的文件了吗？", System.currentTimeMillis() - 2000, sender)
        style.addMessage("好的，我待会儿处理。", System.currentTimeMillis() - 5000, me)
        style.addMessage("你收到我的文件了吗？", System.currentTimeMillis() - 2000, sender)
        style.addMessage("好的，我待会儿处理。", System.currentTimeMillis() - 5000, me)
        style.addMessage("你收到我的文件了吗？", System.currentTimeMillis() - 2000, sender)
        style.addMessage("好的，我待会儿处理。", System.currentTimeMillis() - 5000, me)
        style.addMessage("你收到我的文件了吗？", System.currentTimeMillis() - 2000, sender)
        style.addMessage("好的，我待会儿处理。", System.currentTimeMillis() - 5000, me)
        style.addMessage("你收到我的文件了吗？", System.currentTimeMillis() - 2000, sender)

        // style.addHistoricMessage(NotificationCompat.MessagingStyle.Message("", System.currentTimeMillis() - 5000, me))

        // 4. 添加快捷回复 Action (RemoteInput)
        val replyLabel = "回复"
        val remoteInput = RemoteInput.Builder("KEY_TEXT_REPLY")
            .setLabel(replyLabel)
            .setAllowDataType("image/*", true)  //允许图片
            .build()

        val replyPendingIntent = NotificationUtils.getPendingIntent(this, "REPLY_ACTION", true) // 实际中应指向一个处理回复的 Service 或 BroadcastReceiver

        val replyAction = NotificationCompat.Action.Builder(R.drawable.message_icon, replyLabel, replyPendingIntent)
            .addRemoteInput(remoteInput)
            .build()

        // 5. 创建通知
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(style)
            .addAction(replyAction) // 添加回复操作
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.icon_stick))
            .setVisibility(Notification.VISIBILITY_PUBLIC) // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏\
        // 关键：使用 setShortcutId() 关联到之前创建的快捷方式 ID, 可提升排序等, 且需和message绑定使用

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            createShortcut()
            builder.setShortcutId("conversation_shortcut_john")
        }

        builder.setGroup("Group_Message")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyCall() {
        // 1. 创建服务 Intent
        val serviceIntent = Intent(this, CallNotificationService::class.java)

        // 2. 启动前台服务
        // 根据 Android 版本使用不同的启动方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android O (API 26) 及更高版本必须使用 startForegroundService
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun showNotifyCall2() {
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

        // --- 2. 定义全屏意图 ---
        // 这是实现“来电”效果的关键：通知将启动一个全屏 Activity
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java)
        val fullScreenPendingIntent =
            PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // --- 3. 定义接听和拒接操作 ---
        // 假设这些意图会发送到 BroadcastReceiver 或 Service 来处理接听/拒接逻辑
        val acceptIntent = NotificationUtils.getPendingIntent(this, "CALL_ACCEPT", true)
        val rejectIntent = NotificationUtils.getPendingIntent(this, "CALL_REJECT", true)

        val acceptAction = NotificationCompat.Action.Builder(R.drawable.music_play, "接听", acceptIntent).build()
        val rejectAction = NotificationCompat.Action.Builder(R.drawable.music_pause, "拒绝", rejectIntent).build()

        // --- 4. 构建通知 ---
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话传入语音电话")
            .setContentText("来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三来电人：张三")
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.message_icon))
            // 关键：设置 Full-Screen Intent
            .setFullScreenIntent(fullScreenPendingIntent, true) // 第二个参数 true 表示它是全屏的
            // 关键：设置高优先级以确保浮动通知（Heads-up）
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // 关键：将通知设置为“正在进行中”，阻止其被清除
            .setOngoing(true)
            // 添加操作按钮
            .addAction(acceptAction)
            .addAction(rejectAction)
        builder.setGroup("Group_Call2")
        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyProgress() {
        val CHANNEL_ID = "Channel_Progress"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "进度条样式通知"
            val descriptionText = "进度条样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val NOTIFICATION_ID = generateNotifyId()
        val maxProgress = 100
        var currentProgress = 0
        val progressStyle = NotificationCompat.ProgressStyle()
        progressStyle.setProgress(0)
        progressStyle.setProgressIndeterminate(false)
        progressStyle.setStyledByProgress(true)
        val segments = listOf(
            NotificationCompat.ProgressStyle.Segment(0).apply {
                color = Color.RED
            },
            NotificationCompat.ProgressStyle.Segment(25).apply {
                color = Color.BLUE
            },
            NotificationCompat.ProgressStyle.Segment(50).apply {
                color = Color.GRAY
            },
            NotificationCompat.ProgressStyle.Segment(75).apply {
                color = Color.GREEN
            },
            NotificationCompat.ProgressStyle.Segment(100).apply {
                color = Color.CYAN
            }
        )
        progressStyle.setProgressSegments(segments)
        val points = listOf(
            NotificationCompat.ProgressStyle.Point(0).apply {
                color = Color.YELLOW
            },
            NotificationCompat.ProgressStyle.Point(25).apply {
                color = Color.BLACK
            },
            NotificationCompat.ProgressStyle.Point(50).apply {
                color = Color.CYAN
            },
            NotificationCompat.ProgressStyle.Point(75).apply {
                color = Color.DKGRAY
            },
            NotificationCompat.ProgressStyle.Point(100).apply {
                color = Color.MAGENTA
            }
        )
        progressStyle.setProgressPoints(points)
        progressStyle.setProgressStartIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher))
        progressStyle.setProgressTrackerIcon(IconCompat.createWithResource(this, R.drawable.music_album))
        progressStyle.setProgressEndIcon(IconCompat.createWithResource(this, R.drawable.icon_stick))
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中")
            .setContentText("0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成 0% 完成")
            .setStyle(progressStyle)
            .setOngoing(true) // 标记为正在进行的任务，用户无法滑动清除
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())
        builder.setGroup("Group_Progress")

        // 1. 首次发送通知
        // false 表示有明确进度
        // builder.setProgress(maxProgress, currentProgress, false)
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        // 2. 模拟后台线程更新通知进度
        Thread {
            while (currentProgress < maxProgress) {
                SystemClock.sleep(1000) // 模拟每秒下载 10%
                currentProgress += 10

                // 更新通知内容
                builder.setContentText("$currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成$currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成 $currentProgress% 完成")
                // builder.setProgress(maxProgress, currentProgress, false)
                progressStyle.setProgress(currentProgress)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            }

            // 3. 下载完成，更新通知状态并移除进度条
            builder.setContentTitle("下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成")
                .setContentText("文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存文件已保存")
                // .setProgress(0, 0, false) // max=0, progress=0, false 会隐藏进度条
                .setOngoing(false) // 允许用户清除通知
            progressStyle.setProgress(maxProgress)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }.start()
    }

    private fun showNotifyProgress2() {
        val CHANNEL_ID = "Channel_Progress"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "进度条样式2通知"
            val descriptionText = "进度条样式2通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val NOTIFICATION_ID = generateNotifyId()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中文件下载中")
            .setOngoing(true) // 表示这是一个持续的任务
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())

        // 演示无明确进度条 (Indeterminate)
        // builder.setContentText("正在连接...").setProgress(0, 0, true)
        // notificationManager.notify(NOTIFICATION_ID, builder.build())

        // 演示有明确进度条，如下载任务
        val maxProgress = 100
        var currentProgress = 0
        builder.setProgress(maxProgress, currentProgress, false) // false表示有明确进度
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        // 实际应用中，您会在后台线程中循环更新进度
        while (currentProgress <= maxProgress) {
            Thread.sleep(500) // 模拟下载延迟
            currentProgress += 10
            builder.setProgress(maxProgress, currentProgress, false)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }

        // 下载完成后，移除进度条并更新为完成状态
        builder.setContentText("下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成下载完成")
        builder.setProgress(0, 0, false)
        builder.setGroup("Group_Progress2")

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun showNotifyMedia() {
        val intent = Intent(this, MediaPlaybackServiceCompat::class.java).apply {
            action = "ACTION_START_PLAYBACK"
        }
        // 使用 startForegroundService 启动服务，确保在 5 秒内调用 startForeground
        ContextCompat.startForegroundService(this, intent)
    }

    private fun showNotifyMedia2() {
        val CHANNEL_ID = "Channel_Media"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "媒体样式通知"
            val descriptionText = "媒体样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 1. 创建操作按钮
        val playAction = NotificationCompat.Action(R.drawable.music_play, "播放", NotificationUtils.getPendingIntent(this, "PLAY_ACTION"))
        val prevAction = NotificationCompat.Action(R.drawable.music_previous, "上一曲", NotificationUtils.getPendingIntent(this, "PREV_ACTION"))
        val nextAction = NotificationCompat.Action(R.drawable.music_next, "下一曲", NotificationUtils.getPendingIntent(this, "NEXT_ACTION"))
        val cancelIntent = NotificationUtils.getPendingIntent(this, "CANCEL_PLAY")

        // 2. 创建通知
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
        mediaStyle.setShowActionsInCompactView(0, 1, 2) // 在收起视图中显示这三个按钮 (索引 0, 1, 2)
        mediaStyle.setShowCancelButton(true)
        mediaStyle.setCancelButtonIntent(cancelIntent)
        mediaStyle.setRemotePlaybackInfo("远程设备名称", R.drawable.icon_stick, NotificationUtils.getPendingIntent(this, "CANCEL_PLAY"))
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌我的歌")
            .setContentText("艺术家名称艺术家名称艺术家名称艺术家名称艺术家名称艺术家名称艺术家名称艺术家名称艺术家名称艺术家名称艺术家名称")
            .setSubText("专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称专辑名称")
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.music_album))
            // 3. 添加操作按钮
            .addAction(prevAction)
            .addAction(playAction)
            .addAction(nextAction)
            // 4. 应用 MediaStyle
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 媒体通知通常是 LOW 或 DEFAULT
            .setOngoing(true) // 播放时应持续显示
        builder.setGroup("Group_Media2")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyMedia3() {
        val CHANNEL_ID = "Channel_Media"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "媒体样式通知"
            val descriptionText = "媒体样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 1. 创建操作按钮
        val playAction = NotificationCompat.Action(R.drawable.music_play, "播放", NotificationUtils.getPendingIntent(this, "PLAY_ACTION"))
        val prevAction = NotificationCompat.Action(R.drawable.music_previous, "上一曲", NotificationUtils.getPendingIntent(this, "PREV_ACTION"))
        val nextAction = NotificationCompat.Action(R.drawable.music_next, "下一曲", NotificationUtils.getPendingIntent(this, "NEXT_ACTION"))
        val favAction = NotificationCompat.Action(R.drawable.music_fav, "收藏", NotificationUtils.getPendingIntent(this, "FAV_ACTION"))
        val closeAction = NotificationCompat.Action(R.drawable.music_close, "关闭", NotificationUtils.getPendingIntent(this, "CLOSE_ACTION"))
        val cancelIntent = NotificationUtils.getPendingIntent(this, "CANCEL_PLAY")

        // 2. 创建通知
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
        mediaStyle.setShowActionsInCompactView(0, 1, 2) // 在收起视图中显示这三个按钮 (索引 0, 1, 2)

        val mediaSession = MediaSessionCompat(this, "MediaNotification")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                // 处理播放逻辑
            }

            override fun onPause() {
                super.onPause()
                // 处理暂停逻辑
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                // 处理下一曲逻辑
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                // 处理上一曲逻辑
            }
        })
        // 激活 MediaSession
        mediaSession.isActive = true
        setupMediaSession()
        mediaStyle.setMediaSession(mediaSession.sessionToken) // 实际应用中会绑定一个 MediaSessionToken
        mediaStyle.setShowCancelButton(true)
        mediaStyle.setCancelButtonIntent(cancelIntent)
        mediaStyle.setRemotePlaybackInfo("远程设备名称", R.drawable.icon_stick, NotificationUtils.getPendingIntent(this, "CANCEL_PLAY"))
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setContentTitle("我的歌")
            .setContentText("艺术家名称")
            .setSubText("专辑名称")
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.music_album))
            // 3. 添加操作按钮
            .addAction(prevAction)
            .addAction(playAction)
            .addAction(nextAction)
            .addAction(favAction)
            .addAction(closeAction)
            // 4. 应用 MediaStyle
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 媒体通知通常是 LOW 或 DEFAULT
            .setOngoing(true) // 播放时应持续显示
        builder.setGroup("Group_Media3")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyFull() {
        val CHANNEL_ID = "Channel_Full"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "全屏样式通知"
            val descriptionText = "全屏样式通知描述"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            channel.setShowBadge(true) // 启用角标
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val longText =
            "这是一段非常长的文本内容示例。当通知展开时，用户可以看到所有这些文本，而不仅仅是标准通知中的一行摘要。"
        val bidTextStyle = NotificationCompat.BigTextStyle()
        bidTextStyle.bigText(longText) // 设置展开后显示的全部文本
        bidTextStyle.setBigContentTitle("完整新闻标题") // 可选：替换展开时的标题
        bidTextStyle.setSummaryText("来自应用XX") // 可选：设置摘要尾部文本
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.icon_stick))
            .setContentTitle("新闻摘要")
            .setContentText("点击展开查看全文") // 摘要文本
            .setSubText("次要内容")
            .setStyle(bidTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOnlyAlertOnce(true)
            .setTimeoutAfter(10000)
            .setUsesChronometer(true)
            // 关键设置 1: 设置角标上的数字
            // 这会同时设置通知角标的数字和通知中心的数字。
            // 如果设置为 0，则通常会隐藏角标。
            .setNumber(5)
            // 关键设置 2: 设置角标的图标类型（可选，API 26+）
            // NotificationCompat.BADGE_ICON_SMALL：使用通知的小图标 (setSmallIcon)
            // NotificationCompat.BADGE_ICON_LARGE：使用通知的大图标 (setLargeIcon)
            // NotificationCompat.BADGE_ICON_NONE：不显示角标（会覆盖通道的设置）
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
        builder.setGroup("Group_Full")

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyCustom() {
        val CHANNEL_ID = "Channel_Call"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "自定义样式通知"
            val descriptionText = "自定义样式通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 1. 创建自定义 RemoteViews 实例
        val foldView = RemoteViews(this.packageName, R.layout.custom_notification_fold)
        val headsUpView = RemoteViews(this.packageName, R.layout.custom_notification_headsup)
        val expandView = RemoteViews(this.packageName, R.layout.custom_notification_expand)

        // 2. 创建一个操作按钮 (Action)
        val cancelIntent = PendingIntent.getBroadcast(this, 0, Intent("ACTION_CANCEL_UPLOAD"), PendingIntent.FLAG_IMMUTABLE)
        val cancelAction = NotificationCompat.Action.Builder(R.drawable.music_pause, "取消上传", cancelIntent).build()

        // 3. 构建通知
        val customStyle = NotificationCompat.DecoratedCustomViewStyle()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            // 关键：即使使用了自定义视图，也需要设置基本标题和内容作为备用或辅助信息
            .setContentTitle("系统通知装饰区标题")
            .setContentText("这是自定义视图上方的系统保留区域")
            // 关键：设置自定义内容视图
            .setCustomContentView(foldView)
            .setCustomHeadsUpContentView(headsUpView)
            .setCustomBigContentView(expandView)
            // 关键：应用 DecoratedCustomViewStyle
            .setStyle(customStyle)
            // 添加操作按钮，它会显示在自定义内容视图的下方
            .addAction(cancelAction)
            .setOngoing(true) // 上传任务通常是持续性的
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setGroup("Group_Custom")

        builder.setCategory(Notification.CATEGORY_MESSAGE)
        val style = NotificationCompat.MessagingStyle("")
        style.addMessage("伪装消息", System.currentTimeMillis(), "伪装消息")
        builder.setStyle(style)

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createShortcut() {
        // 1. 获取 ShortcutManager
        val shortcutManager = this.getSystemService(ShortcutManager::class.java)

        // 2. 定义唯一的快捷方式 ID
        val SHORTCUT_ID = "conversation_shortcut_john"

        // 3. 构建 Intent (用户点击快捷方式时要执行的操作)
        val shortcutIntent = Intent(this, IncomingCallActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("user_id", "john_doe")
        }

        // 4. 构建 Person 对象 (用于 MessagingStyle 的最佳实践)
        val person = android.app.Person.Builder()
            .setName("John Doe")
            .setKey("john_doe_key") // 用于 MessagingStyle
            .build()

        // 5. 构建 ShortcutInfo
        val shortcut = ShortcutInfo.Builder(this, SHORTCUT_ID)
            .setShortLabel("John Doe")
            .setLongLabel("与 John Doe 的对话")
            .setIcon(Icon.createWithResource(this, R.drawable.message_icon))
            .setIntent(shortcutIntent)
            // 关键：将 Person 对象关联到快捷方式 (API 29+)
            .setPerson(person)
            .setLongLived(true)
            .build()

        // 6. 发布或更新快捷方式
        shortcutManager.dynamicShortcuts = listOf(shortcut)
    }

    private fun showNotifyPush() {
        val CHANNEL_ID = "Channel_Push_Big"
        val GROUY_KEY = "Group_Push_Big"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "Push通知(大图)"
            val descriptionText = "Push通知(大图)描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Breaking News"
        val text = "Workers in 19 states get pay boost as minimum wages jump nationwide on New Year's Day"
        val subText = "Phoenix"
        val phxIcon = IconCompat.createWithResource(this, R.drawable.ic_launcher_phx)
        val largeIcon = IconCompat.createWithResource(this, R.drawable.largeicon)
        val largePicBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.largeicon)
        val bigPicBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.push_icon)

        val titleSpanBuilder = SpannableString(title)
        titleSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        titleSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textSpanBuilder = SpannableString(text)
        textSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        textSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val bigPicStyle = NotificationCompat.BigPictureStyle()
        bigPicStyle.setBigContentTitle(titleSpanBuilder)
        bigPicStyle.setSummaryText(textSpanBuilder)
        bigPicStyle.bigPicture(bigPicBitmap)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bigPicStyle.showBigPictureWhenCollapsed(true)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            // .setSmallIcon(getSmallIcon()) // 必须设置的小图标
            .setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
            .setShowWhen(true)
            .setContentTitle(titleSpanBuilder)
            .setContentText(textSpanBuilder)
            // .setSubText(subText)
            .setStyle(bigPicStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // .setLargeIcon(bigPicBitmap)
            // .setLargeIcon(largePicBitmap)
            // .setLargeIcon(BitmapFactory.decodeResource(this.resources, android.R.drawable.ic_dialog_info))
            // .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            // .setNumber(5)
            // .setColor(Color.RED)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setContentIntent(NotificationUtils.getPendingIntent(this, "com.onus.demotest.ACTION_OPEN_NOTIFICATION"))
            .setOngoing(false)
            // .setTicker("AAA")
            .setUsesChronometer(false)  //记时往后
            // .setLights(Color.RED, 1000, 1000)
            // .setGroupSummary(true)
            // .setGroupAlertBehavior(Notification.GROUP_ALERT_ALL)
            .setLocalOnly(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
        // .setCategory(NotificationCompat.CATEGORY_STATUS)
        // .setBubbleMetadata(NotificationCompat.BubbleMetadata.Builder())

        builder.setGroup(GROUY_KEY)

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyPush2(needShortcut: Boolean) {
        val CHANNEL_ID = "Channel_Push_Message"
        val GROUY_KEY = "Group_Push_Message"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "Push通知(消息)"
            val descriptionText = "Push通知(消息)描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Breaking News"
        val text = "Workers in 19 states get pay boost as minimum wages jump nationwide on New Year's Day"
        val subText = "Phoenix"
        val phxIcon = IconCompat.createWithResource(this, R.drawable.ic_launcher_phx)
        val largeIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.largeicon)
        val largeIcon = IconCompat.createWithAdaptiveBitmap(largeIconBitmap)
        val largePicBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.largeicon)
        val bigPicBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.push_icon)
        val phxBitmap: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_phx)

        val titleSpanBuilder = SpannableString(title)
        titleSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        titleSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textSpanBuilder = SpannableString(text)
        textSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        textSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val titleUser = Person.Builder()
            .setName(titleSpanBuilder)
            .setImportant(false)
            .setBot(false)
            .setIcon(largeIcon)
            .build()

        // val messageStyle = NotificationCompat.MessagingStyle(titleUser)
        val messageStyle = NotificationCompat.MessagingStyle(titleUser)
        messageStyle.conversationTitle = titleSpanBuilder
        messageStyle.isGroupConversation = false
        val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Uri.parse("android.resource://" + packageName + "/" + R.drawable.push_icon)
        } else {
            Uri.parse("file:///storage/emulated/0/push_icon.jpeg")
        }

        val textMessage = NotificationCompat.MessagingStyle.Message(textSpanBuilder, System.currentTimeMillis(), titleUser)
        messageStyle.addMessage(textMessage)
        val imgMessage = NotificationCompat.MessagingStyle.Message("", System.currentTimeMillis(), titleUser)
        imgMessage.setData("image/*", imageUri)
        messageStyle.addMessage(imgMessage)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            // .setSmallIcon(R.mipmap.ic_launcher)
            // .setSmallIcon(R.drawable.smallicon_status, 10000) // 必须设置的小图标
            .setSmallIcon(getSmallIcon())
            // .setSmallIcon(R.drawable.ic_launcher_phx)
            // .setSmallIcon(largeIcon)
            // .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setShowWhen(true)
            // .setContentTitle(title)
            // .setContentText(text)
            // .setContentText(spanBuilder)
            .setSubText(title)
            .setStyle(messageStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // .setLargeIcon(bigPicBitmap)
            // .setLargeIcon(largePicBitmap)
            // .setLargeIcon(BitmapFactory.decodeResource(this.resources, android.R.drawable.ic_dialog_info))
            // .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            // .setNumber(5)
            // .setColor(Color.RED)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setContentIntent(NotificationUtils.getPendingIntent(this, "com.onus.demotest.ACTION_OPEN_NOTIFICATION"))
            .setOngoing(false)
            // .setTicker("AAA")
            .setUsesChronometer(false)  //记时往后
            // .addPerson(titleUser)
            // .setLights(Color.RED, 1000, 1000)
            // .setGroupSummary(true)
            // .setGroupAlertBehavior(Notification.GROUP_ALERT_ALL)
            .setLocalOnly(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
        // .setCategory(NotificationCompat.CATEGORY_STATUS)
        // .setBubbleMetadata(NotificationCompat.BubbleMetadata.Builder())

        // if (needShortcut && Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        //     val locusId = LocusId(title)
        //     builder.setLocusId(LocusIdCompat.toLocusIdCompat(locusId))
        // }

        val SHORTCUT_ID = "shortcut_push_message"
        if (needShortcut && Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            val shortcutManager = getSystemService(SHORTCUT_SERVICE) as ShortcutManager
            val shortcut = createShortcut(this, SHORTCUT_ID, title, largeIcon, getSafeLaunchIntent(this))
            val oldShort = shortcutManager.dynamicShortcuts.find { it.id == SHORTCUT_ID }
            if (oldShort != null) {
                shortcutManager.updateShortcuts(listOf(shortcut.toShortcutInfo()))
            } else {
                shortcutManager.addDynamicShortcuts(listOf(shortcut.toShortcutInfo()))
            }

            builder.setShortcutId(SHORTCUT_ID)
        }

        builder.setGroup(GROUY_KEY)

        notificationManager.notify(generateNotifyId(), builder.build())

        if (needShortcut && Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            removeCurrentShotCut(this, listOf(SHORTCUT_ID))
        }
    }

    private fun showNotifyClean(needLargeIcon: Boolean) {
        val CHANNEL_ID = "Channel_Clean_Default"
        val GROUY_KEY = "Group_Clean_Default"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "清理(基础)通知"
            val descriptionText = "清理(基础)通知描述"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val cleanTitle = "17.9M junk files found"
        val cleanText = "Clean to release space"
        val clearIcon = IconCompat.createWithResource(this, R.drawable.notification_ticker_basic_clean_icon)
        val clearBitmap = BitmapFactory.decodeResource(this.resources, R.drawable.notification_ticker_basic_clean_icon)

        val titleSpanBuilder = SpannableString(cleanTitle)
        titleSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        titleSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textSpanBuilder = SpannableString(cleanText)
        textSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        textSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val contentIntent = NotificationUtils.getPendingIntent(this, "com.onus.demotest.ACTION_OPEN_NOTIFICATION")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(getSmallIcon())
            .setShowWhen(true)
            .setContentTitle(titleSpanBuilder)
            .setContentText(textSpanBuilder)
            // .setSubText(subText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            // .setLargeIcon(largePicBitmap)
            // .setLargeIcon(BitmapFactory.decodeResource(this.resources, android.R.drawable.ic_dialog_info))
            // .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            // .setNumber(5)
            // .setColor(Color.RED)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            // .setTicker("AAA")
            .setUsesChronometer(false)  //记时往后
            // .setLights(Color.RED, 1000, 1000)
            // .setGroupSummary(true)
            // .setGroupAlertBehavior(Notification.GROUP_ALERT_ALL)
            .setLocalOnly(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
        // .setCategory(NotificationCompat.CATEGORY_STATUS)

        if (needLargeIcon) {
            builder.setLargeIcon(clearBitmap)
        }

        val actionText = SpannableString("Clean now")
        actionText.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        actionText.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        builder.addAction(NotificationCompat.Action.Builder(0, actionText, NotificationUtils.getPendingIntent(this, "Clean")).build())

        // val actionBuilder = NotificationCompat.Action.Builder(0, "测试", NotificationUtils.getPendingIntent(this, "测试"))
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        //     actionBuilder.setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_CALL)
        // }
        // builder.addAction(actionBuilder.build())

        builder.setGroup(GROUY_KEY)

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyClean2() {
        val CHANNEL_ID = "Channel_Clean_Message"
        val GROUY_KEY = "Group_Clean_Message"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "清理(消息)通知"
            val descriptionText = "清理(消息)通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val cleanTitle = "17.9M junk files found"
        val cleanText = "Clean to release space"
        val clearIcon = IconCompat.createWithResource(this, R.drawable.notification_ticker_basic_clean_icon)
        val phxIcon = IconCompat.createWithResource(this, R.drawable.notification_ticker_alpha_icon)

        val titleSpanBuilder = SpannableString(cleanTitle)
        titleSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        titleSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textSpanBuilder = SpannableString(cleanText)
        textSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        textSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val contentIntent = NotificationUtils.getPendingIntent(this, "com.onus.demotest.ACTION_OPEN_NOTIFICATION")

        val user = Person.Builder()
            .setName(titleSpanBuilder)
            .setImportant(false)
            .setBot(false)
            .setIcon(clearIcon)
            .build()

        val messageStyle = NotificationCompat.MessagingStyle(user)
        // messageStyle.conversationTitle = titleSpanBuilder
        messageStyle.isGroupConversation = false
        val textMessage = NotificationCompat.MessagingStyle.Message(textSpanBuilder, System.currentTimeMillis(), user)
        messageStyle.addMessage(textMessage)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(getSmallIcon())
            .setShowWhen(true)
            // .setContentTitle(title)
            // .setContentText(text)
            // .setContentText(spanBuilder)
            // .setSubText(subText)
            .setStyle(messageStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // .setLargeIcon(bigPicBitmap)
            // .setLargeIcon(largePicBitmap)
            // .setLargeIcon(BitmapFactory.decodeResource(this.resources, android.R.drawable.ic_dialog_info))
            // .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            // .setNumber(5)
            // .setColor(Color.RED)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            // .setTicker("AAA")
            .setUsesChronometer(false)  //记时往后
            .addPerson(user)
            // .setLights(Color.RED, 1000, 1000)
            // .setGroupSummary(true)
            // .setGroupAlertBehavior(Notification.GROUP_ALERT_ALL)
            .setLocalOnly(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
        // .setCategory(NotificationCompat.CATEGORY_STATUS)

        // if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        //     val locusId = LocusId(title)
        //     builder.setLocusId(LocusIdCompat.toLocusIdCompat(locusId))
        // }

        val SHORTCUT_ID = "shortcut_clean_message"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            val shortcutManager = getSystemService(SHORTCUT_SERVICE) as ShortcutManager
            val shortcutIntent = Intent(this, IncomingCallActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                // putExtra("user_id", "john_doe")
            }

            val shortcut = createShortcut(this, SHORTCUT_ID, cleanTitle, clearIcon, getSafeLaunchIntent(this))
            val oldShort = shortcutManager.dynamicShortcuts.find { it.id == SHORTCUT_ID }
            if (oldShort != null) {
                shortcutManager.updateShortcuts(listOf(shortcut.toShortcutInfo()))
            } else {
                shortcutManager.addDynamicShortcuts(listOf(shortcut.toShortcutInfo()))
            }
            builder.setShortcutId(SHORTCUT_ID)
        }

        val actionText = SpannableString("Clean now")
        actionText.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        actionText.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        builder.addAction(NotificationCompat.Action.Builder(0, actionText, NotificationUtils.getPendingIntent(this, "Clean")).build())

        // val actionBuilder = NotificationCompat.Action.Builder(0, "测试", NotificationUtils.getPendingIntent(this, "测试"))
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        //     actionBuilder.setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_CALL)
        // }
        // builder.addAction(actionBuilder.build())

        builder.setGroup(GROUY_KEY)

        // notificationManager.notify(generateNotifyId(), builder.build())
        notificationManager.notify("news", generateNotifyId(), builder.build())

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            removeCurrentShotCut(this, listOf(SHORTCUT_ID))
        }
    }

    private fun showNotifyClean3() {
        val CHANNEL_ID = "Channel_Clean_Message"
        val GROUY_KEY = "Group_Clean_Message"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "清理(消息)通知"
            val descriptionText = "清理(消息)通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val cleanTitle = "17.9M junk files found"
        val cleanText = "Clean to release space"
        val clearIcon = IconCompat.createWithResource(this, R.drawable.notification_ticker_basic_clean_icon)
        val phxIcon = IconCompat.createWithResource(this, R.drawable.notification_ticker_alpha_icon)

        val htmlTitle = Html.fromHtml("<font color='red'>${cleanTitle}</font>")
        val htmlText = Html.fromHtml("<font color='red'>${cleanText}</font>")
        val titleSpanBuilder = SpannableString(cleanTitle)
        titleSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        titleSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textSpanBuilder = SpannableString(cleanText)
        textSpanBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        textSpanBuilder.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val contentIntent = NotificationUtils.getPendingIntent(this, "com.onus.demotest.ACTION_OPEN_NOTIFICATION")

        val user = Person.Builder()
            .setName(titleSpanBuilder)
            .setImportant(false)
            .setBot(false)
            .setIcon(clearIcon)
            .build()

        val messageStyle = NotificationCompat.MessagingStyle(user)
        messageStyle.addMessage(NotificationCompat.MessagingStyle.Message(textSpanBuilder, System.currentTimeMillis(), user))
        // val messageStyle = NotificationCompat.MessagingStyle("")
        // messageStyle.addMessage(NotificationCompat.MessagingStyle.Message(textSpanBuilder, System.currentTimeMillis(), titleSpanBuilder))

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(getSmallIcon())
            .setShowWhen(true)
            // .setContentTitle(title)
            // .setContentText(text)
            // .setContentText(spanBuilder)
            // .setSubText(subText)
            .setStyle(messageStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.file_clean_basic_icon_for_notify))
            // .setNumber(5)
            // .setColor(Color.RED)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            // .setTicker("AAA")
            .setUsesChronometer(false)  //记时往后
            // .addPerson(user)
            // .setLights(Color.RED, 1000, 1000)
            // .setGroupSummary(true)
            // .setGroupAlertBehavior(Notification.GROUP_ALERT_ALL)
            .setLocalOnly(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
        // .setCategory(NotificationCompat.CATEGORY_STATUS)

        // if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        //     val locusId = LocusId(title)
        //     builder.setLocusId(LocusIdCompat.toLocusIdCompat(locusId))
        // }

        val actionText = SpannableString("Clean now")
        actionText.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        actionText.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        actionText.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        builder.addAction(NotificationCompat.Action.Builder(0, actionText, NotificationUtils.getPendingIntent(this, "Clean")).build())

        // val actionBuilder = NotificationCompat.Action.Builder(0, "测试", NotificationUtils.getPendingIntent(this, "测试"))
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        //     actionBuilder.setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_CALL)
        // }
        // builder.addAction(actionBuilder.build())

        builder.setGroup(GROUY_KEY)

        notificationManager.notify(generateNotifyId(), builder.build())
    }

    private fun showNotifyGroup() {
        val CHANNEL_ID = "Channel_Group"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "分组通知"
            val descriptionText = "分组通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val NOTIFICATION_ID = generateNotifyId()
        val GROUP_KEY = "group_summery"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
        builder.setContentTitle("分组通知标题${NOTIFICATION_ID}")
        val spanBuilder = SpannableString("分组通知内容${NOTIFICATION_ID}")
        spanBuilder.setSpan(StyleSpan(Typeface.BOLD), 5, 10, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        builder.setContentText(spanBuilder)

        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.addAction(NotificationCompat.Action.Builder(R.drawable.music_play, "测试", NotificationUtils.getPendingIntent(this, "测试")).build())

        builder.setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.img_beauty))  // 折叠是图标

        //享受高优先级，但不弹headsup
        builder.setGroup(GROUP_KEY)//设置分组
        builder.setGroupSummary(false)//告诉系统我不是折叠头
        builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)//设置只有折叠头的时候才需要headsup

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        val SUMMARY_ID = 98765
        val summaryBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        summaryBuilder.setSmallIcon(R.mipmap.ic_launcher) // 必须设置的小图标
        summaryBuilder.setStyle(NotificationCompat.InboxStyle().setSummaryText("您有多条新消息")) // 摘要描述
        summaryBuilder.setGroup(GROUP_KEY) // 关键：必须与上面的 Key 一致
        summaryBuilder.setGroupSummary(true) // 关键：声明这是摘要通知
        notificationManager.notify(SUMMARY_ID, summaryBuilder.build())
    }

    fun getSmallIcon(): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            R.drawable.notification_ticker_alpha_icon
        } else {
            R.drawable.notification_ticker_icon
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcut(
        context: Context,
        shortcutId: String,
        title: String,
        icon: IconCompat,
        targetIntent: Intent
    ): ShortcutInfoCompat {

        val person = Person.Builder()
            .setName(title)
            .setIcon(icon)
            .setBot(false)
            .setImportant(true)
            .build()

        return ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(title)
            .setPerson(person)
            .setCategories(hashSetOf(ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION))
            .setIcon(icon)
            .setIntent(targetIntent)
            .setLongLived(true) //这句话非常重要,只有加了这句话，通知才能真正的排在前面去
            .setLocusId(LocusIdCompat(shortcutId))
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun removeCurrentShotCut(context: Context, shortcutIds: List<String>) {
        val shortcutManager = getSystemService(SHORTCUT_SERVICE) as ShortcutManager
        // shortcutManager.removeAllDynamicShortcuts()
        shortcutManager.removeDynamicShortcuts(shortcutIds)
    }

    fun getSafeLaunchIntent(context: Context): Intent {
        val intent = Intent()
        val action = context.packageName + ".action.VIEW_IN_CURRENT"
        intent.component = ComponentName(context, "com.onus.demotest.feature.notification.NotifyDemoActivity")
        intent.action = action
        intent.setPackage(context.packageName)
        return intent
    }

    companion object {
        private const val REQUEST_CODE = 1000

        private var notifyId = 1000
        fun generateNotifyId(): Int {
            return notifyId++
        }
    }
}