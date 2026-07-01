package com.onus.demotest.common

import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @Author: onuszhao
 * @Date: 2024-01-18 20:09
 * @Description:
 */
object LogUtils {

    var resultFile: File? = null

    fun log(msg: String) {
        resultFile?.let { file ->
            var writer = FileWriter(file.path, true)
            kotlin.runCatching {
                val curTime =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).format(Date())
                writer.write("$curTime : $msg\n")
                writer.flush()
            }.onFailure {
                it.printStackTrace()
            }

            kotlin.runCatching {
                writer.close()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun createTestFile() {
        var download: File? = null
        kotlin.runCatching {
            download =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        }.onFailure {
            download = File("/storage/emulated/0/Download")
        }

        download?.let {
            kotlin.runCatching {
                if (!it.exists()) {
                    it.mkdir()
                }

                val file = File(it, "demo_test.txt")
                if (file.exists()) {
                    file.delete()
                }

                file.createNewFile()
                resultFile = file
                log("创建文件成功：${file.path}")
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

}