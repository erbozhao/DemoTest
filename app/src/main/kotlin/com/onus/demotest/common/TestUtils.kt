package com.onus.demotest.common

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.app.Activity
import com.onus.demotest.data.ShareFileProvider
import com.onus.demotest.core.threadpool.CVExecutorSupplier
import com.onus.demotest.core.threadpool.orderly.CVHandler
import com.onus.demotest.core.threadpool.orderly.CVHandlerType
import com.onus.demotest.core.threadpool.orderly.CVMessage
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.util.Calendar
import java.util.concurrent.CountDownLatch

/**
 * @Author: onuszhao
 * @Date: 2024-09-19 17:42
 * @Description:
 */
object TestUtils {

    private fun Context.startActivityCompat(intent: Intent) {
        if (this !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun testTimeVersion() {
        val curTime = System.currentTimeMillis()
        val calendar1 = Calendar.getInstance().apply { timeInMillis = curTime }
        calendar1.set(Calendar.HOUR_OF_DAY, 0)
        calendar1.set(Calendar.MINUTE, 0)
        calendar1.set(Calendar.SECOND, 0)
        calendar1.set(Calendar.MILLISECOND, 0)

        val nextTime = System.currentTimeMillis() + (2 * DateUtils.HOUR_IN_MILLIS)
        val calendar2 = Calendar.getInstance().apply { timeInMillis = nextTime }
        calendar2.set(Calendar.HOUR_OF_DAY, 0)
        calendar2.set(Calendar.MINUTE, 0)
        calendar2.set(Calendar.SECOND, 0)
        calendar2.set(Calendar.MILLISECOND, 0)

        val nextTime1 = System.currentTimeMillis() + (5 * DateUtils.DAY_IN_MILLIS)
        val calendar3 = Calendar.getInstance().apply { timeInMillis = nextTime1 }
        calendar3.set(Calendar.HOUR_OF_DAY, 0)
        calendar3.set(Calendar.MINUTE, 0)
        calendar3.set(Calendar.SECOND, 0)
        calendar3.set(Calendar.MILLISECOND, 0)

        val nextTime2 = System.currentTimeMillis() + (10 * DateUtils.DAY_IN_MILLIS)
        val calendar4 = Calendar.getInstance().apply { timeInMillis = nextTime2 }
        calendar4.set(Calendar.HOUR_OF_DAY, 0)
        calendar4.set(Calendar.MINUTE, 0)
        calendar4.set(Calendar.SECOND, 0)
        calendar4.set(Calendar.MILLISECOND, 0)

        val nextTime3 = System.currentTimeMillis() + (15 * DateUtils.DAY_IN_MILLIS)
        val calendar5 = Calendar.getInstance().apply { timeInMillis = nextTime3 }
        calendar5.set(Calendar.HOUR_OF_DAY, 0)
        calendar5.set(Calendar.MINUTE, 0)
        calendar5.set(Calendar.SECOND, 0)
        calendar5.set(Calendar.MILLISECOND, 0)

        val stepTime = 1 * DateUtils.DAY_IN_MILLIS
        val stepTime2 = 10 * DateUtils.DAY_IN_MILLIS
        val stepTime3 = 15 * DateUtils.DAY_IN_MILLIS
        Log.d(
            "onuszhao", "calendar1=${calendar1.timeInMillis}  calendar2=${calendar2.timeInMillis}  calendar3=${calendar3.timeInMillis}  " +
                "calendar4=${calendar4.timeInMillis}  calendar5=${calendar5.timeInMillis}  stepTime=${stepTime}"
        )
        Log.d(
            "onuszhao",
            "calendar1=${calendar1.timeInMillis / stepTime}  calendar2=${calendar2.timeInMillis / stepTime}  calendar3=${calendar3.timeInMillis / stepTime}  " +
                "calendar4=${calendar4.timeInMillis / stepTime}  calendar5=${calendar5.timeInMillis / stepTime}"
        )
        Log.d(
            "onuszhao",
            "calendar1=${calendar1.timeInMillis / stepTime2}  calendar2=${calendar2.timeInMillis / stepTime2}  calendar3=${calendar3.timeInMillis / stepTime2}  " +
                "calendar4=${calendar4.timeInMillis / stepTime2}  calendar5=${calendar5.timeInMillis / stepTime2}"
        )
        Log.d(
            "onuszhao",
            "calendar1=${calendar1.timeInMillis / stepTime3}  calendar2=${calendar2.timeInMillis / stepTime3}  calendar3=${calendar3.timeInMillis / stepTime3}  " +
                "calendar4=${calendar4.timeInMillis / stepTime3}  calendar5=${calendar5.timeInMillis / stepTime3}"
        )
    }

    fun testJsonPares() {
        val jsonStr = "[{\"scene\":3,\n" +
            "\"intervalDay\":7,\n" +
            "\"maxCount\":3},\n" +
            "{\"scene\":1,\n" +
            "\"intervalDay\":7,\n" +
            "\"maxCount\":3},\n" +
            "{\"scene\":18,\n" +
            "\"intervalDay\":7,\n" +
            "\"maxCount\":3},\n" +
            "{\"scene\":2,\n" +
            "\"intervalDay\":7,\n" +
            "\"maxCount\":3}]"
        kotlin.runCatching {
            val array = JSONArray(jsonStr)
            val length = array.length()
            for (i in 0 until length) {
                val json = array.getJSONObject(i)
                val scene = json.optInt("scene")
                val intervalDay = json.optInt("intervalDay")
                val maxCount = json.optInt("maxCount")
                Log.d("onuszhao", "scene=$scene  intervalDay=$intervalDay  maxCount=$maxCount")
            }
        }

        val test = ""
        val channels = JsonUtils.getStringFromJson(test, "channels", "").split("|")
        val resetTime = JsonUtils.getStringFromJson(test, "resetTime", "60").toLongOrNull() ?: 60L
        Log.d("onuszhao", "channels=$channels  resetTim=$resetTime")

        val test1 = "{\"channels\":\"a|b|c\",\"resetTime\":60}"
        val channels1 = JsonUtils.getStringFromJson(test1, "channels", "").split("|")
        val resetTime1 = JsonUtils.getStringFromJson(test1, "resetTime", "60").toLongOrNull() ?: 60L
        Log.d("onuszhao", "channels1=$channels1  resetTime1=$resetTime1")
    }

    fun testListToStr() {
        val list = mutableListOf<String>().apply {
            add("1")
            add("2")
            add("3")
        }
        Log.d("onuszhao", "${list}  ${list.joinToString(",")}")
    }

    fun testListEqual() {
        Log.d("onuszhao", "${TextUtils.equals(null, null)}")

        val list1: List<String> = listOf("a", "b", "c")
        val list2: List<String> = listOf("c", "b", "a")
        Log.d("onuszhao", "1  ${list1 == list2}")
        Log.d("onuszhao", "2  ${list1.toSet() == list2.toSet()}")
        Log.d("onuszhao", "3  ${list1.sorted() == list2.sorted()}")
    }

    fun testOpenChannel(context: Context) {
        val CHANNEL_ID = "notify_test_channel_1"
        try {
            var intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
                intent.putExtra("app_package", context.packageName);
                intent.putExtra("app_uid", context.applicationInfo.uid)
            } else {
                intent.action = Settings.ACTION_SETTINGS
            }
            context.startActivityCompat(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun testOpenNovel(context: Context) {
        //启动小说
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // intent.setPackage("com.cloudview.novel")
        intent.`package` = "com.cloudview.novel"
        intent.component = ComponentName("com.cloudview.novel", "com.cloudview.novel.MainActivity")
        context.startActivityCompat(intent)
    }

    fun testShareFile(context: Context) {
        // val filePath = "/storage/emulated/0/doc/document_doc_1MB-119.doc" // 本地文件路径
        // val filePath = "/storage/emulated/0/other/sample-2.7z" // 本地文件路径
        // val filePath = "/storage/emulated/0/video/sample_960x540.3gp" // 本地文件路径
        // val file = File(filePath)
        // val fileUri = ShareFileProvider.getUriForFile(applicationContext, file)
        // // val fileUri = Uri.fromFile(file) // 将文件路径转换为 Uri 对象
        // val intent = Intent(Intent.ACTION_VIEW)
        // intent.setData(fileUri)
        // // intent.setDataAndType(fileUri, "application/octet-stream")
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // startActivity(intent)

        val intent = Intent(Intent.ACTION_VIEW)
        val file = File("/storage/emulated/0/Download/test.mp3")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uri = ShareFileProvider.getUriForFile(context, file)
            intent.data = uri
        } else {
            intent.data = Uri.fromFile(file)
        }

        // intent.setDataAndType(Uri.parse("/test/test.MP3"), type)
        // intent.setDataAndType(Uri.fromFile(file), type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        context.startActivityCompat(intent)
    }

    fun testTarget34(context: Context) {
        // val fileName = "test_zip_abnormal"  // test_zip_normal.zip  test_zip_abnormal.zip
        // val zipFilePath = "/storage/emulated/0/$fileName.zip"
        // val destDir = "/storage/emulated/0/$fileName"
        // ZipUtils.unzip(zipFilePath, destDir)

        // (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.let {
        //     it.killBackgroundProcesses("com.transsion.phoenix")
        // }

        // applicationContext.registerReceiver(object : BroadcastReceiver() {
        //     override fun onReceive(context: Context?, intent: Intent) {
        //         Log.d("onuszhao", "onReceive")
        //         // val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        //         // val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        //         // val batteryPct = level / scale.toFloat()
        //         // Log.d("onuszhao", "Battery Level: " + batteryPct * 100 + "%")
        //     }
        // }, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            Log.d("onuszhao", "canUseFullScreenIntent=${manager?.canUseFullScreenIntent()}")
            // val uri = Uri.parse("package:" + packageName)
            // var intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, uri)
            // startActivity(intent)

            // val intent = Intent()
            // intent.action = Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
            // intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            // startActivity(intent)

            // val intent = Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT")
            // intent.addCategory("android.intent.category.DEFAULT")
            // intent.data = Uri.parse("package:" + packageName)
            // startActivity(intent)

            // val inputStream = assets.open("channel.ini")
            // val reader = BufferedReader(InputStreamReader(inputStream))
            // val stringBuilder = StringBuilder()
            // var line: String?
            // while (reader.readLine().also { line = it } != null) {
            //     stringBuilder.append(line)
            // }
            // val fileContent = stringBuilder.toString()
            // inputStream.close()
            // Log.d("onuszhao", "fileContent=$fileContent")

            // 复制文件
            try {
                Log.d(
                    "onuszhao",
                    "filesDir=${context.filesDir}  dataDir=${context.dataDir}  cacheDir=${context.cacheDir}  =${
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    }"
                )
                val dstFile = File(context.filesDir, "downloadEngine.jar")
                val inputStream = context.assets.open("downloadEngine.jar")
                val outputStream: OutputStream = FileOutputStream(dstFile)
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val jar = File(context.filesDir, "downloadEngine.jar")
            jar.setReadOnly()
            val parentClassLoader = context.classLoader
            val cl = PathClassLoader(jar.absolutePath, parentClassLoader)
            val outPath = context.getDir("dynamic_jar_output", Context.MODE_PRIVATE)
            val classLoader = DexClassLoader(jar.absolutePath, outPath.absolutePath, "", parentClassLoader)
        }
    }

    fun testConnect(success: () -> Unit, failed: () -> Unit) {
        doConnectPlayStore({
            success.invoke()
        }, { code, msg ->
            retryConnect(1, 2, 1000, success, failed)
        })
    }

    private fun retryConnect(curRetryCount: Int, maxRetryCount: Int, retryInitialTime: Long, success: () -> Unit, failed: () -> Unit) {
        kotlin.runCatching {
            val delayTime = retryInitialTime * curRetryCount
            Thread.sleep(delayTime)

            doConnectPlayStore({
                success.invoke()
            }, { code, msg ->
                if (curRetryCount < maxRetryCount) {
                    retryConnect(curRetryCount + 1, maxRetryCount, retryInitialTime, success, failed)
                } else {
                    failed.invoke()
                }
            })
        }.onFailure {
            if (curRetryCount < maxRetryCount) {
                retryConnect(curRetryCount + 1, maxRetryCount, retryInitialTime, success, failed)
            } else {
                failed.invoke()
            }
        }
    }

    private fun doConnectPlayStore(success: () -> Unit, failed: (Int, String?) -> Unit) {
        kotlin.runCatching {
            success.invoke()
        }.onFailure {
            failed.invoke(1, "")
        }
    }

    fun testConcurrentEp() {
        // GlobalScope.launch(Dispatchers.IO) {
        //     // 在这里执行后台任务
        //     println("Running in background thread")
        //     // 模拟长时间操作
        //     delay(1000)
        //     // 切换到主线程更新 UI
        //     withContext(Dispatchers.Main) {
        //         println("Update UI on main thread")
        //     }
        // }

        val list = mutableListOf<String>().apply {
            add("1")
            add("2")
            add("3")
        }
        // val tread1 = object : Thread() {
        //     override fun run() {
        //         list.forEachIndexed { index, s ->
        //             // if (index % 5 == 0) {
        //             //     Log.d("onuszhao", "remove item ${index}  str=$s")
        //             //     list.removeAt(index)
        //             // }
        //             Log.d("onuszhao", "remove item ${index}  str=$s")
        //         }
        //     }
        // }
        // val tread2 = object : Thread() {
        //     override fun run() {
        //         for (i in 0 until 100) {
        //             list.add(0, i.toString())
        //             Log.d("onuszhao", "add ${i}")
        //         }
        //     }
        // }
        // for (i in 0 until 100) {
        //     tread1.start()
        // }
        // tread2.start()

        for (i in 0 until 100) {
            GlobalScope.launch(Dispatchers.IO) {
                list.forEachIndexed { index, s ->
                    Log.d("onuszhao", "cur item ${index}  $s")
                }
            }
        }

        for (i in 0 until 100) {
            GlobalScope.launch(Dispatchers.IO) {
                list.add(i.toString())
                Log.d("onuszhao", "add ${i}")
            }
        }
    }

    val handler = CVHandler(CVHandlerType.SHORT_TIME_THREAD)

    fun testHandler() {
        Log.d("onuszhao", "testHandler  1")
        handler.post {
            Log.d("onuszhao", "testHandler  2")
            // CVExecutorSupplier.forBackgroundTasks().execute {
            Log.d("onuszhao", "testHandler  3")
            getData {
                Log.d("onuszhao", "testHandler  4")
                handler.post {
                    Thread.sleep(6000)
                    Log.d("onuszhao", "testHandler  5")
                }
            }
            Thread.sleep(8000)
            // }
            Log.d("onuszhao", "testHandler  6")
        }
    }

    fun testHandler2() {
        handler.postDelayed({
            Log.d("onuszhao", "exec  1")
        }, 8000)
        Log.d("onuszhao", "add  1")
        handler.postDelayed({
            Log.d("onuszhao", "exec  2")
        }, 2000)
        Log.d("onuszhao", "add  2")
        handler.postDelayed({
            Log.d("onuszhao", "exec  3")
        }, 5000)
        Log.d("onuszhao", "add  3")
    }

    private fun getData(success: () -> Unit) {
        CVExecutorSupplier.forBackgroundTasks().execute {
            Thread.sleep(6000)
            success.invoke()
        }
    }

    private const val SYNC_MESSAGE1 = 1
    private const val SYNC_MESSAGE2 = 1
    private val syncHandler = CVHandler(CVHandlerType.IO_THREAD, object : CVHandler.Callback {
        override fun handleMessage(msg: CVMessage): Boolean {
            val time = kotlin.random.Random.nextInt(10000).toLong()
            Log.d("onuszhao", "start  handleMessage  ${msg.obj}  time=$time")
            // Thread.sleep(3000)
            Thread.sleep(time)
            Log.d("onuszhao", "end  handleMessage  ${msg.obj}  time=$time")
            return true
        }
    })

    private val syncHandler2 = CVHandler(CVHandlerType.IO_THREAD, object : CVHandler.Callback {
        override fun handleMessage(msg: CVMessage): Boolean {
            val time = kotlin.random.Random.nextInt(10000).toLong()
            Log.d("onuszhao", "start  handleMessage  ${msg.obj}  time=$time")
            // Thread.sleep(3000)
            Thread.sleep(time)
            Log.d("onuszhao", "end  handleMessage  ${msg.obj}  time=$time")
            return true
        }
    })

    fun testHandMessage() {
        syncHandler.removeMessages(SYNC_MESSAGE1)
        syncHandler2.removeMessages(SYNC_MESSAGE2)
        syncHandler.sendMessage(syncHandler.obtainMessage(SYNC_MESSAGE1, "test 1"))
        syncHandler2.sendMessage(syncHandler2.obtainMessage(SYNC_MESSAGE2, "test syncHandler2 1"))
        syncHandler.sendMessage(syncHandler.obtainMessage(SYNC_MESSAGE1, "test 2"))
        syncHandler2.sendMessage(syncHandler2.obtainMessage(SYNC_MESSAGE2, "test syncHandler2 2"))
        syncHandler.sendMessage(syncHandler.obtainMessage(SYNC_MESSAGE1, "test 3"))
        syncHandler2.sendMessage(syncHandler2.obtainMessage(SYNC_MESSAGE2, "test syncHandler2 3"))
        // syncHandler.sendMessageDelayed(syncHandler.obtainMessage(SYNC_MESSAGE1, "test 3"), 6000)
        syncHandler.sendMessage(syncHandler.obtainMessage(SYNC_MESSAGE1, "test 4"))
        syncHandler2.sendMessage(syncHandler2.obtainMessage(SYNC_MESSAGE2, "test syncHandler2 4"))
        syncHandler.sendMessage(syncHandler.obtainMessage(SYNC_MESSAGE2, "test 5"))
        syncHandler2.sendMessage(syncHandler2.obtainMessage(SYNC_MESSAGE2, "test syncHandler2 5"))
        syncHandler.sendMessage(syncHandler.obtainMessage(SYNC_MESSAGE2, "test 6"))
        syncHandler2.sendMessage(syncHandler2.obtainMessage(SYNC_MESSAGE2, "test syncHandler2 6"))
        // syncHandler.sendMessageDelayed(syncHandler.obtainMessage(SYNC_MESSAGE2, "test 6"), 6000)
        syncHandler.sendMessage(syncHandler.obtainMessage(SYNC_MESSAGE2, "test 7"))
        syncHandler2.sendMessage(syncHandler2.obtainMessage(SYNC_MESSAGE2, "test syncHandler2 7"))
    }

    fun testForBreak() {
        label@ for (text in 0..9) {
            Log.d("onuszhao", "text $text")
            for (line in 10..19) {
                Log.d("onuszhao", "line $line")
                for (column in 20..29) {
                    Log.d("onuszhao", "column $column")
                    if (column == 21) {
                        break@label
                    }
                }
            }
        }
    }

    fun testMD5() {
        Log.d("onuszhao", "${"test".toMD5()}")
        Log.d("onuszhao", "${"test".toMD5()}")
        Log.d("onuszhao", "${"1234dfgdfsgergerg".toMD5()}")
        Log.d("onuszhao", "${"1234dfgdfsgergerg".toMD5()}")
        Log.d("onuszhao", "${"1234dfgdfsgergerg".substring(0, 0)}")
    }

    fun testRequest() {
        request1 {
            Log.d("onuszhao", "request1")
        }
        request2 {
            Log.d("onuszhao", "request2")
        }
    }

    private fun request1(callback: () -> Unit) {
        CVExecutorSupplier.forBackgroundTasks().execute {
            Thread.sleep(5000)
            callback.invoke()
        }
    }

    private fun request2(callback: () -> Unit) {
        CVExecutorSupplier.forBackgroundTasks().execute {
            callback.invoke()
        }
    }

    fun testByLazy() {
        Log.d("onuszhao", "testByLazy  fontSizeLevel=$fontSizeLevel")
    }

    private val fontSizeLevel by lazy(LazyThreadSafetyMode.NONE) {
        val level = 14
        if (level < 1) 1 else if (level > 16) 16 else level
    }

    fun testSize() {
        val list = mutableListOf<String>()
        repeat(130) { index ->
            list.add("${index}")
        }
        val segmentLists = list.chunked(129)
        segmentLists.reversed().forEachIndexed { index, list ->
            Log.d("onuszhao", "segmentList index=${index}  size=${list.size} ${list.first()}:${list.last()}")
        }

        // val map = mutableMapOf<String, Int>()
        // map.put("1", 5)
        // map.put("2", 1)
        // map.put("3", 2)
        // Log.d("onuszhao", "size   key=${map.keys.size}  value=${map.values.sum()}")
        //
        // val list = mutableListOf<String>().apply {
        //     add("1")
        //     add("2")
        // }
        // val list2 = mutableListOf<String>().apply {
        //     add("1")
        //     add("2")
        //     add("3")
        //     add("4")
        //     add("5")
        // }
        // Log.d(
        //     "onuszhao",
        //     " list=${if (list.size >= 3) list.subList(0, 3) else list}  list2=${list2.subList(0, 3)} list2=${list2.subList(3, list2.size)}"
        // )
        //
        //
        // Log.d("onuszhao", " plus=${-1 + 0} ")
        //
        // val testList = mutableListOf<String>().apply {
        //     repeat(10) { index ->
        //         add("$index")
        //     }
        // }
        // Log.d("onuszhao", "swap  init=${testList}")
        // Collections.swap(testList, 6, 2)
        // Log.d("onuszhao", "swap  init=${testList}")
        //
        // val removeList = mutableListOf<String>().apply {
        //     repeat(10) { index ->
        //         add("$index")
        //     }
        // }
        // Log.d("onuszhao", "swap  init=${removeList.remove("123")}")
    }

    fun testGetBrandInfo() {
        val brand = Build.BRAND
        val model = Build.MODEL
        Log.d("onuszhao", "brand=${brand}  model=${model}")
    }

    fun testGetThirdChar() {
        val str1: String? = null
        val str2 = "e01"
        val str3 = "e0"
        val str4 = "e018eb94-3142-4c92-9686-eecc3bcd609e"
        val thirdChar1 = if (!str1.isNullOrEmpty() && str1.length > 3) str1.substring(2, 3) else ""
        val thirdChar2 = if (!str2.isNullOrEmpty() && str2.length > 3) str2.substring(2, 3) else ""
        val thirdChar3 = if (!str3.isNullOrEmpty() && str3.length > 3) str3.substring(2, 3) else ""
        val thirdChar4 = if (!str4.isNullOrEmpty() && str4.length > 3) str4.substring(2, 3) else ""
        Log.d(
            "onuszhao",
            "testGetThirdChar   thirdChar=${thirdChar1}   thirdChar2=${thirdChar2}   thirdChar3=${thirdChar3}   thirdChar4=${thirdChar4}  is=${thirdChar4 == "2"}"
        )
    }

    fun testBatchProcess() {
        val ids = mutableListOf<String>().apply {
            repeat(1030) { index ->
                add("${index}")
            }
        }
        batchProcess(ids) { subIds ->
            Log.d("onuszhao", "batchProcess  ${subIds}")
        }
        Log.d("onuszhao", "batchProcess  end")
    }

    fun testForAsnc() {
        Log.d("onuszhao", "testTemp start")

        val filteredPurchases = listOf("test01", "test02", "test03")
        var completedCount = 0
        for (i in filteredPurchases.indices) {
            Log.d("onuszhao", "consumePurchases start")

            var hasFailed = false
            val latch = CountDownLatch(1) // 用于等待回调完成的计数器
            CVExecutorSupplier.forBackgroundTasks().execute {
                Thread.sleep(500)
                Log.d("onuszhao", "consumePurchases ${filteredPurchases[i]}")
                synchronized(this) {
                    completedCount++

                    if (i == 1) {
                        hasFailed = true
                        Log.d("onuszhao", "consumePurchases failed")
                    }
                }
                Log.d("onuszhao", "consumePurchases ${filteredPurchases[i]}  end")
                latch.countDown() // 异步操作完成后，计数器减一
            }
            latch.await() // 阻塞当前线程，直到计数器归零（即回调完成）

            if (hasFailed) {
                Log.d("onuszhao", "consumePurchases failed")
                break
            }
            Log.d("onuszhao", "consumePurchases end")
        }

        Log.d("onuszhao", "testTemp end")
    }

    private fun batchProcess(ids: List<String>, block: (List<String>) -> Unit) {
        val stepNum = 100
        var j = 0
        while (j < ids.size) {
            var endPosition = j + stepNum
            if (endPosition > ids.size) {
                endPosition = ids.size
            }
            val subChapterIds = ids.subList(j, endPosition)
            block(subChapterIds)
            j += stepNum
        }
    }
}

fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray()) // 将字符串转为字节并计算 MD5
    return digest.joinToString("") { "%02x".format(it) } // 转为16进制字符串
}
