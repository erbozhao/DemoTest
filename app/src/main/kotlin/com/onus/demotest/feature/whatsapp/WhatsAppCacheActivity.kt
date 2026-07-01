package com.onus.demotest.feature.whatsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.R
import com.onus.demotest.data.ShareFileProvider
import com.onus.demotest.common.CacheUtils
import com.onus.demotest.common.DeviceUtils
import com.onus.demotest.common.FileUtils
import com.onus.demotest.common.LogUtils
import java.io.File

/**
 * @Author: onuszhao
 * @Date: 2023-06-26 17:38
 * @Description:
 */
class WhatsAppCacheActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var whatsAppCacheButton: Button
    private lateinit var shareButton: Button
    private lateinit var resultContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_whatsapp_cache)
        ActivityStateManager.setCurActivity(this)
        findViewById<View?>(R.id.rootView)?.setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this), 0, 0)

        findViewById<Button?>(R.id.whatsAppCache)?.let {
            whatsAppCacheButton = it
            it.setOnClickListener(this@WhatsAppCacheActivity)
        }
        findViewById<Button?>(R.id.shareResult)?.let {
            shareButton = it
            it.setOnClickListener(this@WhatsAppCacheActivity)
        }
        resultContainer = findViewById(R.id.result)
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v.id) {
            R.id.whatsAppCache -> {
                requestFilePermission()
            }
            R.id.shareResult -> {
                shareResultFile()
            }
            else -> {
            }
        }
    }

    private fun requestFilePermission() {
        // 检查是否已经授予管理外部存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivityForResult(intent, PERMISSIONS_CODE_MANAGE_EXTERNAL_STORAGE)
                LogUtils.log("请求文件权限")
            } else {
                LogUtils.createTestFile()
                grantDocumentPermissionIfNeed()
            }
        } else {
            // 检查文件读写权限
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(this@WhatsAppCacheActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@WhatsAppCacheActivity, permissions, PERMISSIONS_CODE_EXTERNAL_STORAGE)
                    LogUtils.log("请求文件权限")
                    return
                }
            }

            LogUtils.createTestFile()
            grantDocumentPermissionIfNeed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_CODE_EXTERNAL_STORAGE && ActivityCompat.checkSelfPermission(this@WhatsAppCacheActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "文件读写权限已开启", Toast.LENGTH_SHORT).show()
            LogUtils.createTestFile()
            LogUtils.log("文件读写权限已开启")
            grantDocumentPermissionIfNeed()
        } else {
            Toast.makeText(applicationContext, "文件读写权限获取失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONS_CODE_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()){
                Toast.makeText(applicationContext, "文件读写权限已开启", Toast.LENGTH_SHORT).show()
                LogUtils.createTestFile()
                LogUtils.log("文件读写权限已开启")
                grantDocumentPermissionIfNeed()
            } else {
                Toast.makeText(applicationContext, "文件读写权限获取失败", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_CODE_ANDROID_DATA_PERMISSION) {
            data?.data?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                getWhatsAppCache()
                return
            }

            Toast.makeText(applicationContext, "获取Data路径读写权限失败", Toast.LENGTH_SHORT).show()
            LogUtils.log("获取Data路径读写权限失败")
        } else if (requestCode == REQUEST_CODE_ANDROID_DATA_WHATSAPP_PERMISSION) {
            data?.data?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                getWhatsAppCache()
                return
            }

            Toast.makeText(applicationContext, "获取Data路径读写权限失败", Toast.LENGTH_SHORT).show()
            LogUtils.log("获取Data路径读写权限失败")
        } else if (requestCode == REQUEST_CODE_SHARE) {
            Toast.makeText(applicationContext, "分享成功", Toast.LENGTH_SHORT).show()
        }
    }

    private fun grantDocumentPermissionIfNeed() {
        LogUtils.log("grantDocumentPermissionIfNeed sdk=${Build.VERSION.SDK_INT}")
        if (Build.VERSION.SDK_INT >= 33) {
            if (hasWhatsAppPermission()) {
                LogUtils.log("已有Android/data权限")
                getWhatsAppCache()
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, androidDataWhatsAppUri)
                startActivityForResult(intent, REQUEST_CODE_ANDROID_DATA_WHATSAPP_PERMISSION)
                LogUtils.log("请求Android/data权限")
            }
        } else if (Build.VERSION.SDK_INT >= 30 && Build.VERSION.SDK_INT < 33) {
            if (hasWhatsAppPermission()) {
                LogUtils.log("已有Android/data权限")
                getWhatsAppCache()
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, androidDataUri)
                startActivityForResult(intent, REQUEST_CODE_ANDROID_DATA_PERMISSION)
                LogUtils.log("请求Android/data权限")
            }
        } else {
            getWhatsAppCache()
        }
    }

    private fun hasWhatsAppPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val persistedUriPermissions = contentResolver.persistedUriPermissions
            val androidDataUri = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"
            val androidDataWhatsAppUri = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.whatsapp"
            for (persistedUriPermission in persistedUriPermissions) {
                LogUtils.log("hasWhatsAppPermission uri=${persistedUriPermission.uri}")
                if ((TextUtils.equals(persistedUriPermission.uri.toString(), androidDataUri) || TextUtils.equals(persistedUriPermission.uri.toString(), androidDataWhatsAppUri))
                    && persistedUriPermission.isWritePermission && persistedUriPermission.isReadPermission) {
                    return true
                }
            }
            return false
        } else {
            return ActivityCompat.checkSelfPermission(this@WhatsAppCacheActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getWhatsAppCache() {
        var hasPermission = hasWhatsAppPermission()
        LogUtils.log("getWhatsAppCache hasPermission=$hasPermission")
        if (!hasPermission) {
            return
        }

        if (Build.VERSION.SDK_INT >= 33) {
            val uri = CacheUtils.getUri("/storage/emulated/0/Android/data/com.whatsapp")
            val whatsAppCacheUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri))
            LogUtils.log("getWhatsAppCache whatsAppCacheUri=${whatsAppCacheUri}")
            var logLines = mutableListOf<String>()
            val result = CacheUtils.getDataFromUri(contentResolver, whatsAppCacheUri)
            result.forEach {
                val msg = "${it.path}  ${if (it.isFolder) "目录" else "文件"}  ${it.size}"
                logLines.add(msg)
                LogUtils.log(msg)
                Log.d("onuszhao", "path=${it.path}  isFolder=${it.isFolder} time=${it.modifyTime}  size=${it.size}")
            }
            for (logLine in logLines) {
                createResultLine(logLine)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT < 33) {
            val androidDataUri = CacheUtils.getAndroidDataUri(contentResolver)
            androidDataUri.forEach { pkgName, uri ->
                LogUtils.log("getWhatsAppCache pkgName=${pkgName}  uri=$uri")
                if (TextUtils.equals(pkgName, "com.whatsapp")){
                    // CacheUtils.getAndroidDataCacheUri(contentResolver, uri)?.let { cacheUri ->
                        var logLines = mutableListOf<String>()
                        val result = CacheUtils.getDataFromUri(contentResolver, uri)
                        result.forEach {
                            val msg = "${it.path}  ${if (it.isFolder) "目录" else "文件"}  ${it.size}"
                            logLines.add(msg)
                            LogUtils.log(msg)
                            Log.d("onuszhao", "path=${it.path}  isFolder=${it.isFolder} time=${it.modifyTime}  size=${it.size}")
                        }
                        for (logLine in logLines) {
                            createResultLine(logLine)
                        }
                    // }
                }
            }
        } else {
            val dataFile = File("/storage/emulated/0/Android/data/")
            dataFile.listFiles()?.forEach { pkgDir ->
                LogUtils.log("getWhatsAppCache pkgDir=${pkgDir.path}")
                if (pkgDir.isDirectory && pkgDir.name.equals("com.whatsapp")) {
                    var logLines = mutableListOf<String>()
                    FileUtils.listFileRecursive(pkgDir.path).forEach {
                        val msg = "${it.path}  ${if (it.isDirectory) "目录" else "文件"}  ${it.length()}"
                        logLines.add(msg)
                        LogUtils.log(msg)
                        Log.d("onuszhao", "path=${it.path}  isFolder=${it.isDirectory}  time=${it.lastModified()}  size=${it.length()}")
                    }
                    for (logLine in logLines) {
                        createResultLine(logLine)
                    }
                }
            }
        }
    }

    private fun createResultLine(str: String){
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

    private fun shareResultFile(){
        LogUtils.resultFile?.let {
            val fileExt = FileUtils.getFileExtension(it.path)
            var mimeType = FileUtils.getMimeTypeFromExtension(fileExt, "*/*")
            if ("*/*" == mimeType) {
                mimeType = FileUtils.getUnknownMimeType(fileExt)
            }

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = mimeType
            shareIntent.putExtra(Intent.EXTRA_STREAM, ShareFileProvider.getUriForFile(this, it) ?: Uri.fromFile(it))

            val chooser = Intent.createChooser(shareIntent, null)
            startActivityForResult(chooser, REQUEST_CODE_SHARE)
            return
        }

        Toast.makeText(applicationContext, "未发现结果文件", Toast.LENGTH_SHORT).show()
    }

    companion object{
        val PERMISSIONS_CODE_EXTERNAL_STORAGE = View.generateViewId()
        val PERMISSIONS_CODE_MANAGE_EXTERNAL_STORAGE = View.generateViewId()
        val REQUEST_CODE_ANDROID_DATA_PERMISSION = View.generateViewId()
        val REQUEST_CODE_ANDROID_DATA_WHATSAPP_PERMISSION = View.generateViewId()
        val REQUEST_CODE_SHARE = View.generateViewId()

        val androidDataUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata")
        val androidDataWhatsAppUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata%2Fcom.whatsapp")
        // val androidDataWhatsAppUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.whatsapp/document/primary%3AAndroid%2Fdata%2Fcom.whatsapp")
    }
}