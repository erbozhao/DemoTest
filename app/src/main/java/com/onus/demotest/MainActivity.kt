package com.onus.demotest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.window.SplashScreenView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.onus.demotest.const.WhatsAppFileConst
import com.onus.demotest.core.BuildType
import com.onus.demotest.core.ChannelType
import com.onus.demotest.core.EngineConfigs
import com.onus.demotest.core.KernelEngine
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.pages.file.AllFileActivity
import com.onus.demotest.pages.whatsapp.AllStatusActivity
import com.onus.demotest.pages.notification.BindNotificationActivity
import com.onus.demotest.pages.ui.FontsActivity
import com.onus.demotest.pages.file.UsageStatsActivity
import com.onus.demotest.pages.notification.NotifyDemoActivity
import com.onus.demotest.pages.notification.PostNotificationActivity
import com.onus.demotest.pages.ui.DialogActivity
import com.onus.demotest.pages.webview.WebViewActivity
import com.onus.demotest.pages.ui.tab.TabActivity
import com.onus.demotest.pages.ui.FrameActivity
import com.onus.demotest.pages.ui.draglist.DragListActivity
import com.onus.demotest.pages.video.VideoActivity
import com.onus.demotest.services.EventMessage
import com.onus.demotest.thread.IThreadHandlerFactory
import com.onus.demotest.utils.CommonUtils
import com.onus.demotest.utils.DeviceUtils
import com.onus.demotest.utils.ReflectionUtils
import com.onus.demotest.utils.TestUtils
import com.onus.demotest.view.Toaster
import java.util.concurrent.ThreadFactory

/**
 * @Author: onuszhao
 * @Date: 2023-06-26 17:38
 * @Description:
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // initSplash()
        initService()
        initConfig()

        Handler(Looper.getMainLooper()).postDelayed({
            supportActionBar?.hide()
            setContentView(R.layout.activity_main)
            findViewById<View>(R.id.rootView)?.setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this), 0, 0)

            ActivityStateManager.setCurActivity(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setTheme(R.style.ActivityMain_Light)
            } else {
                setTheme(R.style.ActivityMain)
            }

            initListener()
        }, 100)
    }

    private fun initSplash(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val screenView = this.splashScreen
            screenView.setOnExitAnimationListener { view: SplashScreenView? ->
                (view?.parent as? ViewGroup)?.also {
                    it.removeView(view)
                }
            }
        }
    }

    private fun initService(){
        Log.d("onuszhao", "1  hasBoot=${CommonUtils.hasBoot}  cur=${Thread.currentThread()}")
        val threadHandler = IThreadHandlerFactory.getDefaultFactory().create()
        threadHandler.post{
            val uri = Uri.parse("content://com.onus.demotest.pigeon.service")
            Log.d("onuszhao", "uri=${uri.toString()}")
            val inBundle = Bundle()
            inBundle.putString("key_eventName", "eventName")
            inBundle.putParcelable("key_eventParams", EventMessage())
            applicationContext.contentResolver.call(uri, "call", null, inBundle)


            // val uri  = Uri.Builder()
            // uri.authority("com.onus.demotest.pigeon.service")
            // uri.scheme("content")
            // Log.d("onuszhao", "uri=${uri.build().toString()}")
            // applicationContext.contentResolver.call(uri, "call", null, inBundle)
        }
        // Handler().post{
        //     val uri = Uri.parse("content://com.onus.demotest.pigeon.service")
        //     Log.d("onuszhao", "uri=${uri.toString()}")
        //     val inBundle = Bundle()
        //     inBundle.putString("key_eventName", "eventName")
        //     inBundle.putParcelable("key_eventParams", EventMessage())
        //     applicationContext.contentResolver.call(uri, "call", null, inBundle)
        //
        //     // val uri  = Uri.Builder()
        //     // uri.authority("com.onus.demotest.pigeon.service")
        //     // uri.scheme("content")
        //     // Log.d("onuszhao", "uri=${uri.build().toString()}")
        //     // applicationContext.contentResolver.call(uri, "call", null, inBundle)
        // }
        // Thread {
        //     val uri = Uri.parse("content://com.onus.demotest.pigeon.service")
        //     Log.d("onuszhao", "uri=${uri.toString()}")
        //     val inBundle = Bundle()
        //     inBundle.putString("key_eventName", "eventName")
        //     inBundle.putParcelable("key_eventParams", EventMessage())
        //     applicationContext.contentResolver.call(uri, "call", null, inBundle)
        //
        //     // val uri  = Uri.Builder()
        //     // uri.authority("com.onus.demotest.pigeon.service")
        //     // uri.scheme("content")
        //     // Log.d("onuszhao", "uri=${uri.build().toString()}")
        //     // applicationContext.contentResolver.call(uri, "call", null, inBundle)
        // }.start()
    }

    private fun initConfig(){
        KernelEngine.init(
            EngineConfigs.Builder().setBuildType(
                when (BuildConfig.DEBUG) {
                    true -> BuildType.DEBUG
                    else -> BuildType.RELEASE
                }
            ).setChannelType(
                when (BuildConfig.BUILD_CHANNEL) {
                    23601 -> ChannelType.TRANSSION_BUILDIN
                    30017 -> ChannelType.TRANSSION_PREINSTALL
                    33601 -> ChannelType.VIVO_PREINSTALL
                    33602 -> ChannelType.OPPO_PREINSTALL
                    33610 -> ChannelType.SAMSUNG_PREINSTALL
                    else -> ChannelType.NORMAL
                }
            ).build()
        )

        initFcm()
    }

    private fun initFcm() {
        // 测试https://console.firebase.google.com/u/0/project/erbozhao-70a8f/messaging/onboarding?hl=zh-cn
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("onuszhao", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d("onuszhao", "addOnCompleteListener token=$token")
            Toast.makeText(baseContext, "token=$token", Toast.LENGTH_SHORT).show()
        })
    }

    private fun initListener(){
        // 临时测试
        findViewById<Button?>(R.id.tmpTest)?.setOnClickListener {
            // TestUtils.testTimeVersion()
            // TestUtils.testJsonPares()
            // TestUtils.testListToStr()
            TestUtils.testListEqual()
        }

        findViewById<Button?>(R.id.tmpTest1)?.setOnClickListener {
            // TestUtils.testOpenChannel(applicationContext)
            // TestUtils.testOpenNovel(applicationContext)
            TestUtils.testShareFile(applicationContext)
        }

        findViewById<Button?>(R.id.tmpTest2)?.setOnClickListener {
            // TestUtils.testConnect({}, {})
            // TestUtils.testConcurrentEp()
            // TestUtils.testHandler()
            // TestUtils.testHandler2()
            // TestUtils.testHandMessage()
            // TestUtils.testForBreak()
            // TestUtils.testMD5()
            // TestUtils.testRequest()
            // TestUtils.testByLazy()
            // TestUtils.testSize()
            TestUtils.testGetBrandInfo()
            // TestUtils.testGetThirdChar()
            // TestUtils.testBatchProcess()
            // TestUtils.testForAsnc()
        }

        findViewById<Button?>(R.id.tmpTest3)?.setOnClickListener {
            TestUtils.testTarget34(applicationContext)
        }

        // WhatsApp相关测试
        findViewById<Button?>(R.id.getAllStatus)?.setOnClickListener {
            startActivity(Intent(this, AllStatusActivity::class.java))
        }
        findViewById<Button?>(R.id.openWhatsApp)?.setOnClickListener {
            WhatsAppFileConst.openWhatsApp(applicationContext)
        }
        findViewById<Button?>(R.id.getWhatsAppCache)?.setOnClickListener {
            // 隐式(exported=false时target34会挂)
            // startActivity(Intent("com.example.action.APP_ACTION"))

            // 显示
            val explicitIntent = Intent("com.example.action.APP_ACTION")
            explicitIntent.apply {
                `package` = packageName
            }
            startActivity(explicitIntent)
        }

        // 文件相关测试
        findViewById<Button?>(R.id.getAllFile)?.setOnClickListener {
            startActivity(Intent(this, AllFileActivity::class.java))
        }

        findViewById<Button?>(R.id.testUsageStats)?.setOnClickListener {
            startActivity(Intent(this, UsageStatsActivity::class.java))
        }

        // 网页相关测试
        findViewById<Button?>(R.id.testWebView)?.setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java))
        }

        // 通知相关测试
        findViewById<Button?>(R.id.testBindNotification)?.setOnClickListener {
            startActivity(Intent(this, BindNotificationActivity::class.java))
        }

        findViewById<Button?>(R.id.testToast)?.setOnClickListener {
            // Toaster.show(applicationContext, "test toast  test toast  test toast  test toast", 1500, "View") {
            //     Log.d("onuszhao", "click callback")
            //     Handler(Looper.getMainLooper()).postDelayed({
            //         Toaster.show(applicationContext, "click View", 1500)
            //     }, 2000)
            // }

            Toaster.showSnackBar(applicationContext, window.decorView,"test toast test toast test toast test toast  test toast  test toast", 1500, "View") {
                Log.d("onuszhao", "click callback")
                Toaster.showSnackBar(applicationContext, window.decorView, "click View", 1500)
            }
        }

        findViewById<Button?>(R.id.notifyDemo)?.setOnClickListener {
            startActivity(Intent(this, NotifyDemoActivity::class.java))
        }

        // 视频相关测试
        findViewById<Button?>(R.id.testVideo)?.setOnClickListener {
            startActivity(Intent(this, VideoActivity::class.java))
        }

        findViewById<Button?>(R.id.tabLayout)?.setOnClickListener {
            startActivity(Intent(this, TabActivity::class.java))
        }

        findViewById<Button?>(R.id.fonts)?.setOnClickListener {
            startActivity(Intent(this, FontsActivity::class.java))
        }

        findViewById<Button?>(R.id.frame)?.setOnClickListener {
            startActivity(Intent(this, FrameActivity::class.java))
        }

        findViewById<Button?>(R.id.dialog)?.setOnClickListener {
            startActivity(Intent(this, DialogActivity::class.java))
        }

        findViewById<Button?>(R.id.postNotify)?.setOnClickListener {
            startActivity(Intent(this, PostNotificationActivity::class.java))
        }

        findViewById<Button?>(R.id.dragList)?.setOnClickListener {
            startActivity(Intent(this, DragListActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onuszhao", "MainActivity onDestroy")
        ActivityStateManager.release()
        // killProcess()
    }

    override fun finish() {
        super.finish()
        Log.d("onuszhao", "MainActivity finish")
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("onuszhao", "MainActivity back")
            this.moveTaskToBack(true)
            this.finish()
            release()
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun release() {
        Thread {
            clearGapWorker()
            System.gc()
        }.start()
    }

    private fun clearGapWorker() {
        kotlin.runCatching {
            val sGapWorker = ReflectionUtils.getStaticField("androidx.recyclerview.widget.GapWorker", "sGapWorker")
            if (sGapWorker is ThreadLocal<*>) {
                val recyclerViews = ReflectionUtils.getInstanceField(sGapWorker.get(), "mRecyclerViews")
                if (recyclerViews is ArrayList<*>) {
                    recyclerViews.clear()
                }
            }
        }
    }

    private fun killProcess() {
        Process.killProcess(Process.myPid())
    }
}

private class SimpleThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable?): Thread {
        return Thread(r, "TP_PHX_EVENT_EMITTER")
    }
}

enum class EnumType {
    FILE,
    DOCUMENT,
    WEB_BROWSER,
    DOWNLOAD,
    VIDEO_PLAYER,
    MUSIC_PALER,
}