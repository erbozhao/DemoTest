package com.onus.demotest.feature.whatsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.onus.demotest.R
import com.onus.demotest.core.const.WhatsAppFileConst
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.data.ShareFileProvider
import com.onus.demotest.common.CommonUtils
import com.onus.demotest.common.DeviceUtils
import com.onus.demotest.common.FileUtils
import com.onus.demotest.common.ListUtils
import com.onus.demotest.common.LogUtils
import com.onus.demotest.common.StringUtils
import java.io.File
import java.io.FileFilter

/**
 * @Author: onuszhao
 * @Date: 2024-01-18 20:03
 * @Description:
 */
class AllStatusActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var container: LinearLayout
    private lateinit var requeFilePermission: Button
    private lateinit var getAllStatus: Button
    private lateinit var shareResult: Button
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
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@AllStatusActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        container.addView(wrapper, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        requeFilePermission = Button(this).apply {
            isAllCaps = false
            text = "reqFilePerm"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(com.onus.demotest.R.dimen.dp_5)
            setOnClickListener(this@AllStatusActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        getAllStatus = Button(this).apply {
            isAllCaps = false
            text = "getAllStatus"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@AllStatusActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        shareResult = Button(this).apply {
            isAllCaps = false
            text = "shareResult"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_5)
            setOnClickListener(this@AllStatusActivity)
            wrapper.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
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
        when (v) {
            requeFilePermission -> {
                requestFilePermission()
            }
            getAllStatus -> {
                getAllStatus()
            }
            shareResult -> {
                shareResultFile()
            }
        }
    }

    private fun requestFilePermission() {
        if (!hasFilePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                kotlin.runCatching {
                    val uri = Uri.parse("package:$packageName")
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, uri)
                    startActivityForResult(intent, KEY_REQUEST_CODE)
                }.onFailure {
                    kotlin.runCatching {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivityForResult(intent, KEY_REQUEST_CODE)
                    }.onFailure {
                        val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, KEY_REQUEST_CODE)
                    }
                }
            } else {
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions, KEY_REQUEST_CODE)
            }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == KEY_REQUEST_CODE) {
            Log.d("onuszhao", "permissions=${permissions}  grantResults=$grantResults")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY_REQUEST_CODE) {
            if (hasFilePermission()) {
                Toast.makeText(this, "已获取文件权限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未获取文件权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAllStatus(){
        if (!hasFilePermission()) {
            Toast.makeText(this, "请先获取文件权限", Toast.LENGTH_SHORT).show()
            return
        }

        clearResult()
        LogUtils.createTestFile()

        val result = mutableListOf<File>()
        val statusFiles = WhatsAppFileConst.statusPaths.map { File(it) }
        statusFiles.forEach {
            result.addAll(scanFile(it))
        }

        ListUtils.sortByTime(result)

        result.forEach {
            val type = "文件"
            val path = it.path
            val time = CommonUtils.format(it.lastModified())
            val size = StringUtils.getSizeString(it.length().toFloat(), 1)
            val msg = "$type  $path  $time  $size"
            createResultLine(msg)
            LogUtils.log(msg)
        }
    }

    private fun scanFile(folder: File): List<File> {
        val result = mutableListOf<File>()
        kotlin.runCatching {
            val files = folder.listFiles(FileFilter {
                !it.isHidden
            })
            result.addAll(files)
        }
        return result
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
            startActivity(chooser)
            return
        }

        Toast.makeText(applicationContext, "未发现结果文件", Toast.LENGTH_SHORT).show()
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

    companion object {
        val KEY_REQUEST_CODE = View.generateViewId()
    }
}