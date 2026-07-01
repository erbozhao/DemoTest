package com.onus.demotest.feature.file

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageStats
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.UserHandle
import android.os.storage.StorageManager
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.common.CommonUtils
import com.onus.demotest.common.DeviceUtils
import com.onus.demotest.common.FileUtils
import java.io.File
import java.util.UUID

/**
 * @Author: onuszhao
 * @Date: 2023-10-12 14:33
 * @Description:
 */
class UsageStatsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var container: LinearLayout
    private lateinit var requestFilePermission: Button
    private lateinit var requestUsagePermission: Button
    private lateinit var queryUsageStats: Button
    private lateinit var queryEvents: Button
    private lateinit var queryStatsForPackage: Button
    private lateinit var queryNetwork: Button
    private lateinit var cleanCache: Button
    private lateinit var resultContainer: LinearLayout

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
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@UsageStatsActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)

        requestFilePermission = Button(this).apply {
            isAllCaps = false
            text = "requestFilePermission"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@UsageStatsActivity)
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        requestUsagePermission = Button(this).apply {
            isAllCaps = false
            text = "requestUsagePermission"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@UsageStatsActivity)
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        queryUsageStats = Button(this).apply {
            isAllCaps = false
            text = "queryUsageStats"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@UsageStatsActivity)
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        queryEvents = Button(this).apply {
            isAllCaps = false
            text = "queryEvents"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@UsageStatsActivity)
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        queryStatsForPackage = Button(this).apply {
            isAllCaps = false
            text = "queryStatsForPackage"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@UsageStatsActivity)
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        queryNetwork = Button(this).apply {
            isAllCaps = false
            text = "queryNetwork"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@UsageStatsActivity)
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        cleanCache = Button(this).apply {
            isAllCaps = false
            text = "cleanCache"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@UsageStatsActivity)
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
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
    }

    override fun onClick(v: View?) {
        v ?: return
        clearResult()
        when (v) {
            requestFilePermission -> requestFilePermission()
            requestUsagePermission -> requestUsageStatsPermission()
            queryUsageStats -> queryUsageStats()
            queryEvents -> queryEvents()
            queryStatsForPackage -> queryStatsForPackage()
            queryNetwork -> queryNetwork()
            cleanCache -> cleanCache()
        }
    }

    private fun requestFilePermission() {
        if (!hasFilePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                kotlin.runCatching {
                    val uri = Uri.parse("package:$packageName")
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, uri)
                    startActivityForResult(intent, ID_REQUEST_FILE_PERMISSION)
                }.onFailure {
                    kotlin.runCatching {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivityForResult(intent, ID_REQUEST_FILE_PERMISSION)
                    }.onFailure {
                        val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, ID_REQUEST_FILE_PERMISSION)
                    }
                }
            } else {
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions, ID_REQUEST_FILE_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ID_REQUEST_FILE_PERMISSION) {
            Log.d("onuszhao", "permissions=${permissions}  grantResults=$grantResults")
        }
    }

    private fun hasFilePermission(): Boolean {
        var hasFilePermission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Manifest.permission.MANAGE_EXTERNAL_STORAGE
            hasFilePermission = Environment.isExternalStorageManager()
        } else {
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.forEach {
                hasFilePermission = ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                if (!hasFilePermission) {
                    return@forEach
                }
            }
        }
        return hasFilePermission
    }

    private fun requestUsageStatsPermission() {
        if (!hasPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                kotlin.runCatching {
                    val uri = Uri.parse("package:$packageName")
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, uri)
                    startActivityForResult(intent, ID_REQUEST_USAGE_PERMISSION)
                }.onFailure {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivityForResult(intent, ID_REQUEST_USAGE_PERMISSION)
                }
            }
        }
    }

    private fun hasPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ID_REQUEST_FILE_PERMISSION) {
            if (hasFilePermission()) {
                Toast.makeText(this, "已获取文件权限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未获取文件权限", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == ID_REQUEST_USAGE_PERMISSION) {
            if (hasPermission()) {
                Toast.makeText(this, "已获取PACKAGE_USAGE_STATS权限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未获取PACKAGE_USAGE_STATS权限", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == ID_REQUEST_USAGE_PERMISSION) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_PACKAGE_SIZE) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已获取CLEAR_APP_CACHE权限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未获取CLEAR_APP_CACHE权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 查询应用程序的使用统计数据 */
    private fun queryUsageStats() {
        val recordPkgNames = mutableListOf<String>()
        var usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, Long.MIN_VALUE, Long.MAX_VALUE)
        usageStatsList.forEachIndexed { index, usageStats ->
            val pkgName = usageStats.packageName
            if (recordPkgNames.contains(pkgName)){
                Log.d("onuszhao", "发现重复包 pkgName=$pkgName")
            }else {
                recordPkgNames.add(pkgName)
            }
            val firstEnterTime = usageStats.firstTimeStamp   //应用程序首次进入前台运行的时间戳                --> 每个应用感觉都是系统更新时间
            val lastEnterTime = usageStats.lastTimeStamp  //应用程序最后一次进入前台运行的时间戳(更新的时间)
            val lastUseTime = usageStats.lastTimeUsed //应用程序最后一次进入前台运行的时间(被用户打开或使用的时间)  --> 常用此计算上次使用时间
            val totalUseTime = usageStats.totalTimeInForeground   // 应用程序在前台运行的总时间  -->  常用此计算总使用时间

            var lastTimeVisible = 0L
            var totalTimeVisible = 0L
            var lastForegroundServiceUseTime = 0L
            var totalForegroundServiceUseTime = 0L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                lastTimeVisible = usageStats.lastTimeVisible       // 获取应用最后一次在UI中可见的时间
                totalTimeVisible = usageStats.totalTimeVisible  // 获取此包的活动在UI中可见的总时间
                lastForegroundServiceUseTime = usageStats.lastTimeForegroundServiceUsed  // 获取上次使用此包的前台服务的时间
                totalForegroundServiceUseTime = usageStats.totalTimeForegroundServiceUsed  // 获取此包的前台服务启动的总时间
            }

            val msg =
                "index=$index  pkgName=$pkgName  firstEnterTime=${CommonUtils.format(firstEnterTime)}  lastEnterTime=${CommonUtils.format(lastEnterTime)}  " +
                    "lastUseTime=${CommonUtils.format(lastUseTime)}  totalUseTime=${CommonUtils.millisToMinutes(totalUseTime)}minutes  " +
                    "lastTimeVisible=${CommonUtils.format(lastTimeVisible)}  totalTimeVisible=${CommonUtils.millisToMinutes(totalTimeVisible)}minutes  " +
                    "lastForegroundServiceUseTime=${CommonUtils.format(lastForegroundServiceUseTime)}  totalForegroundServiceUseTime=${
                        CommonUtils.millisToMinutes(
                            totalForegroundServiceUseTime
                        )
                    }minutes"
            Log.d("onuszhao", msg)
            createResultLine(msg)
        }
    }

    private fun queryEvents() {
        var usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * DateUtils.HOUR_IN_MILLIS
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    val msg = "进入前台 ${event.packageName} ${CommonUtils.format(event.timeStamp)}"
                    Log.d("onuszhao", msg)
                    createResultLine(msg)
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val msg = "退出后台 ${event.packageName} ${CommonUtils.format(event.timeStamp)}"
                    Log.e("onuszhao", msg)
                    createResultLine(msg)
                }
            }
        }
    }

    /** 查询目标包名应用是否活跃 */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isAppInactive(context: Context, packageName: String): Boolean {
        val usageStatsManager: UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager.isAppInactive(packageName)
    }

    /** 查询当前应用的使用情况 */
    @RequiresApi(Build.VERSION_CODES.P)
    fun standbyBucket(context: Context) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val standbyBucket = usageStatsManager.appStandbyBucket
        when (standbyBucket) {
            UsageStatsManager.STANDBY_BUCKET_ACTIVE -> {
                Log.e("onuszhao", "当前应用在前台运行")
            }

            UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> {
                Log.e("onuszhao", "当前应用是一个活跃的前台应用，会被频繁的切换到前台")
            }

            UsageStatsManager.STANDBY_BUCKET_FREQUENT -> {
                Log.e("onuszhao", "当前应用是一个常用的后台应用")
            }

            UsageStatsManager.STANDBY_BUCKET_RARE -> {
                Log.e("onuszhao", "当前应用是一个不常用的后台应用")
            }
            //此类型是一个系统级别的被隐藏的类型
//            UsageStatsManager.STANDBY_BUCKET_NEVER -> {
//                Log.e("zh", "当前应用很少被使用")
//            }
        }
    }

    private fun queryStatsForPackage() {
        val installApps = getInstallApps(applicationContext, true, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

            var storageUuid = StorageManager.UUID_DEFAULT // 默认存储空间
            val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumes = storageManager.storageVolumes

            installApps.forEach { appInfo ->
                val pkgName = appInfo.packageName
                val userHandle = UserHandle.getUserHandleForUid(appInfo.uid)

                val packageStats = PackageStats(pkgName)
                storageVolumes.forEach { storageVolume ->
                    if (storageVolume.state.equals(Environment.MEDIA_MOUNTED)) {
                        var uuid = storageVolume.uuid
                        if (uuid != null) {
                            storageUuid = UUID.fromString(uuid)
                        }
                    }
                    val queryStatsForPackage = storageStatsManager.queryStatsForPackage(storageUuid, pkgName, userHandle)
                    val userExternalStorageStats = storageStatsManager.queryExternalStatsForUser(storageUuid, userHandle)
                    if (StorageManager.UUID_DEFAULT == storageUuid) {
                        packageStats.codeSize += queryStatsForPackage.appBytes
                        packageStats.dataSize += queryStatsForPackage.dataBytes - queryStatsForPackage.cacheBytes
                        packageStats.cacheSize += queryStatsForPackage.cacheBytes
                    } else {
                        packageStats.externalCodeSize += userExternalStorageStats.appBytes
                        packageStats.externalDataSize += userExternalStorageStats.audioBytes + userExternalStorageStats.imageBytes + userExternalStorageStats.videoBytes
                        packageStats.externalCacheSize += userExternalStorageStats.totalBytes - (userExternalStorageStats.appBytes + userExternalStorageStats.audioBytes  + userExternalStorageStats.imageBytes + userExternalStorageStats.videoBytes)
                    }
                }

                val codeSize = packageStats.codeSize
                val dataSize = packageStats.dataSize
                val cacheSize = packageStats.cacheSize
                val totalSize = codeSize + dataSize + cacheSize
                val externalDataSize = packageStats.externalCodeSize
                val externalCodeSize = packageStats.externalDataSize
                val externalCacheSize = packageStats.externalCacheSize
                val totalExternalSize = externalDataSize + externalCodeSize + externalCacheSize

                Log.d("onuszhao", "pkgName=$pkgName  codeSize=${CommonUtils.bytesToMegabytes(codeSize)}  dataSize=${CommonUtils.bytesToMegabytes(dataSize)}  cacheSize=${CommonUtils.bytesToMegabytes(cacheSize)}" +
                    " totalSize=${CommonUtils.bytesToMegabytes(totalSize)}  externalDataSize=${CommonUtils.bytesToMegabytes(externalDataSize)}  externalCodeSize=${CommonUtils.bytesToMegabytes(externalCodeSize)}" +
                    " externalCacheSize=${CommonUtils.bytesToMegabytes(externalCacheSize)}  totalExternalSize=${CommonUtils.bytesToMegabytes(totalExternalSize)}")
            }

            // val freeBytes = CommonUtil.bytesToGigabytes(storageStatsManager.getFreeBytes(storageUuid))
            // val totalBytes = CommonUtil.bytesToGigabytes(storageStatsManager.getTotalBytes(storageUuid))
            // val msg1 = "freeBytes=${freeBytes}GB  totalBytes=${totalBytes}GB"
            // Log.d("onuszhao", msg1)
            // createResultLine(msg1)
            //
            // var totalCache = 0L
            // var totalExternalCache = 0L
            // installApps.forEach { appInfo ->
            //     val userHandle = UserHandle.getUserHandleForUid(appInfo.uid)
            //     val storageStats = storageStatsManager.queryStatsForPackage(storageUuid, appInfo.packageName, userHandle)
            //     // val storageStats = storageStatsManager.queryStatsForUid(storageUuid, appInfo.uid)
            //     // val storageStats = storageStatsManager.queryStatsForUid(storageUuid, android.os.Process.myUid())
            //     val appBytes = storageStats.appBytes  // 应用程序占用的存储空间，以字节为单位
            //     val dataBytes = storageStats.dataBytes  // 应用程序的数据占用的存储空间，以字节为单位
            //     val cacheBytes = storageStats.cacheBytes  // 应用程序的缓存占用的存储空间，以字节为单位
            //     val totalBytes = appBytes + dataBytes
            //     totalCache += cacheBytes
            //     var externalCacheBytes = 0L
            //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //         externalCacheBytes = storageStats.externalCacheBytes  // 应用程序的外部存储占用的存储空间，以字节为单位
            //         totalExternalCache += externalCacheBytes
            //     }
            //
            //     val msg2 = "pkgName=${appInfo.packageName}  appBytes=${CommonUtil.bytesToMegabytes(appBytes)}MB  dataBytes=${CommonUtil.bytesToMegabytes(dataBytes)}MB  " +
            //             "cacheBytes=${CommonUtil.bytesToMegabytes(cacheBytes)}MB  externalCacheBytes=${CommonUtil.bytesToMegabytes(externalCacheBytes)}MB  " +
            //             "totalBytes=${CommonUtil.bytesToMegabytes(totalBytes)}MB"
            //     Log.d("onuszhao", msg2)
            //     createResultLine(msg2)
            //
            //     val externalStorageStats = storageStatsManager.queryExternalStatsForUser(storageUuid, userHandle)
            //     val externalAppBytes = CommonUtil.bytesToMegabytes(externalStorageStats.appBytes)
            //     val externalAudioBytes = CommonUtil.bytesToMegabytes(externalStorageStats.audioBytes)
            //     val externalImageBytes = CommonUtil.bytesToMegabytes(externalStorageStats.imageBytes)
            //     val externalVideoBytes = CommonUtil.bytesToMegabytes(externalStorageStats.videoBytes)
            //     val externalTotalBytes = CommonUtil.bytesToMegabytes(externalStorageStats.totalBytes)
            //
            //     val msg3 =
            //         "pkgName=${appInfo.packageName}   externalAppBytes=${externalAppBytes}MB  externalAudioBytes=${externalAudioBytes}MB  externalImageBytes=${externalImageBytes}MB  externalVideoBytes=${externalVideoBytes}MB  externalTotalBytes=${externalTotalBytes}MB"
            //     Log.d("onuszhao", msg3)
            //     createResultLine(msg3)
            // }
            // val msg3 =
            //     "totalCache=${CommonUtil.bytesToMegabytes(totalCache)}MB  totalExternalCache=${CommonUtil.bytesToMegabytes(totalExternalCache)}MB"
            // Log.d("onuszhao", msg3)
            // createResultLine(msg3)
            //
            // val userStorageStats = storageStatsManager.queryStatsForUser(storageUuid, android.os.Process.myUserHandle())
            // val userAppBytes = CommonUtil.bytesToMegabytes(userStorageStats.appBytes)
            // val userDataBytes = CommonUtil.bytesToMegabytes(userStorageStats.dataBytes)
            // val userCacheBytes = CommonUtil.bytesToMegabytes(userStorageStats.cacheBytes)
            // var userExternalCacheBytes = 0.0
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //     userExternalCacheBytes = CommonUtil.bytesToMegabytes(userStorageStats.externalCacheBytes)
            // }
            // val msg4 =
            //     "userAppBytes=${userAppBytes}MB  userDataBytes=${userDataBytes}MB  userCacheBytes=${userCacheBytes}MB  userExternalCacheBytes=${userExternalCacheBytes}MB"
            // Log.d("onuszhao", msg4)
            // createResultLine(msg4)
            //
            // val userExternalStorageStats = storageStatsManager.queryExternalStatsForUser(storageUuid, android.os.Process.myUserHandle())
            // val userExternalTotalBytes = CommonUtil.bytesToMegabytes(userExternalStorageStats.totalBytes)
            // val userExternalAppBytes = CommonUtil.bytesToMegabytes(userExternalStorageStats.appBytes)
            // val userExternalAudioBytes = CommonUtil.bytesToMegabytes(userExternalStorageStats.audioBytes)
            // val userExternalImageBytes = CommonUtil.bytesToMegabytes(userExternalStorageStats.imageBytes)
            // val userExternalVideoBytes = CommonUtil.bytesToMegabytes(userExternalStorageStats.videoBytes)
            // val msg5 =
            //     "userExternalTotalBytes=${userExternalTotalBytes}MB  userExternalAppBytes=${userExternalAppBytes}MB  userExternalAudioBytes=${userExternalAudioBytes}MB  userExternalImageBytes=${userExternalImageBytes}MB  userExternalVideoBytes=${userExternalVideoBytes}MB"
            // Log.d("onuszhao", msg5)
            // createResultLine(msg5)
        } else {
            var totalCacheSize = 0L
            installApps.forEach {
                val pkgCacheSize = getPkgCacheSize(it.packageName)
                totalCacheSize += pkgCacheSize
                val msg = "pkgName=${it.packageName}  pkgCacheSize=${CommonUtils.bytesToMegabytes(pkgCacheSize)}MB"
                Log.d("onuszhao", msg)
                createResultLine(msg)

                // getPackageSize(applicationContext, it.packageName) { stats ->
                //     val codeSize = stats?.codeSize ?: 0    //应用大小
                //     val cacheSize = stats?.cacheSize ?: 0   //缓存大小
                //     val dataSize = stats?.dataSize ?: 0    //数据大小
                //     val externalCacheSize = stats?.externalCacheSize ?: 0    //扩展缓存大小
                //     val totalSize = codeSize + dataSize + cacheSize
                //
                //     val msg = "pkgName=${it.packageName}  codeSize=${codeSize}MB  cacheSize=${cacheSize}MB  dataSize=${dataSize}MB  externalCacheSize=${externalCacheSize}MB"
                //     Log.d("onuszhao", msg)
                //     createResultLine(msg)
                // }
            }

            val msg1 = "totalCacheSize=${CommonUtils.bytesToMegabytes(totalCacheSize)}"
            Log.d("onuszhao", msg1)
            createResultLine(msg1)

            var dirTotalCache = getTotalCacheSize(applicationContext)
            val msg2 = "dirTotalCache=${CommonUtils.bytesToMegabytes(dirTotalCache)}MB"
            Log.d("onuszhao", msg2)
            createResultLine(msg2)
        }
    }

    private fun queryNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var networkStatsManager = getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

            val packageUid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0)).uid
            } else {
                packageManager.getApplicationInfo(packageName, 0).uid
            }
            val endTime = System.currentTimeMillis()    // 查询的结束时间，以毫秒为单位
            val startTime = endTime - 24 * DateUtils.HOUR_IN_MILLIS // 查询的起始时间，以毫秒为单位
            val networkType = ConnectivityManager.TYPE_WIFI // 网络类型，这里以移动网络为例
            val sumNetworkStats = networkStatsManager.querySummary(networkType, packageUid.toString(), startTime, endTime)
            while (sumNetworkStats.hasNextBucket()) {
                val bucket = NetworkStats.Bucket()
                sumNetworkStats.getNextBucket(bucket)

                // 处理当前桶的数据
                val uid = bucket.uid
                val startTime = bucket.startTimeStamp
                val endTime = bucket.endTimeStamp
                val rxBytes = bucket.rxBytes
                val txBytes = bucket.txBytes
                val msg = "total uid=$uid  startTime=$startTime  endTime=$endTime  rxBytes=$rxBytes  txBytes=$txBytes"
                Log.d("onuszhao", msg)
                createResultLine(msg)
            }

            val detailNetworkStats = networkStatsManager.queryDetails(networkType, packageUid.toString(), startTime, endTime)
            while (detailNetworkStats.hasNextBucket()) {
                val bucket = NetworkStats.Bucket()
                detailNetworkStats.getNextBucket(bucket)

                // 处理当前桶的数据
                val uid = bucket.uid
                val startTime = bucket.startTimeStamp
                val endTime = bucket.endTimeStamp
                val rxBytes = bucket.rxBytes
                val txBytes = bucket.txBytes
                val msg = "detail uid=$uid  startTime=$startTime  endTime=$endTime  rxBytes=$rxBytes  txBytes=$txBytes"
                Log.d("onuszhao", msg)
                createResultLine(msg)
            }
        }
    }

    private fun cleanCache() {
        // val intent = Intent(StorageManager.ACTION_CLEAR_APP_CACHE)
        val intent = Intent()
        intent.action = "android.os.storage.action.CLEAR_APP_CACHE"
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityForResult(intent, ID_REQUEST_CACHE_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 从 Android 6.0（API 级别 23）开始，应用程序无法直接清除其他应用程序的缓存
            val pm = applicationContext.packageManager
            // pm.freeStorageAndNotify(cacheSize, object : IPackageDataObserver.Stub() {
            //     override fun onRemoveCompleted(packageName: String?, succeeded: Boolean) {
            //         // 缓存清理完成后的回调
            //         if (succeeded) {
            //             // 缓存清理成功
            //         } else {
            //             // 缓存清理失败
            //         }
            //     }
            // })
        } else {
            val packageName = "com.example.app" // 替换为目标应用程序的包名
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun getInstallApps(context: Context, includeSystem: Boolean = false, includeSelf: Boolean = false): List<ApplicationInfo> {
        val validApps: MutableList<ApplicationInfo> = ArrayList()
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            context.packageManager.getInstalledApplications(0)
        }
        for (app in apps) {
            if (includeSelf || !TextUtils.equals(context.packageName, app.packageName)) {
                if (includeSystem || (app.flags and ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                    validApps.add(app)
                }
            }
        }
        return validApps
    }

    fun getPackageSize(context: Context, pkgName: String, callback: (stats: PackageStats?) -> Unit) {
        kotlin.runCatching {
            // val method =
            //     PackageManager::class.java.getMethod("getPackageSizeInfo", *arrayOf<Class<*>>(String::class.java, IPackageStatsObserver::class.java))
            // // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
            // method.invoke(context.packageManager, pkgName, object : IPackageStatsObserver.Stub() {
            //     override fun onGetStatsCompleted(pStats: PackageStats?, succeeded: Boolean) {
            //         callback.invoke(pStats)
            //     }
            // })
        }.onFailure {
            it.printStackTrace()
        }
    }

    /** 获取整体缓存大小 */
    private fun getTotalCacheSize(context: Context): Long {
        var cacheSize = FileUtils.getFolderSize(context.cacheDir)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += FileUtils.getFolderSize(context.externalCacheDir)
        }
        return cacheSize
    }

    private fun getPkgCacheSize(pkgName: String): Long {
        kotlin.runCatching {
            val cacheFile = File("/storage/emulated/0/Android/data/$pkgName/cache")
            if (cacheFile.exists()) {
                return FileUtils.getFolderSize(cacheFile)
            }
        }.onFailure {
            it.printStackTrace()
        }
        return 0
    }

    private fun clearAllCache(context: Context) {
        FileUtils.deleteFile(context.cacheDir)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            FileUtils.deleteFile(context.externalCacheDir)
        }
    }

    private fun createResultLine(str: String) {
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

    companion object {
        val ID_REQUEST_FILE_PERMISSION = View.generateViewId()
        val ID_REQUEST_USAGE_PERMISSION = View.generateViewId()
        val ID_REQUEST_CACHE_PERMISSION = View.generateViewId()
    }
}