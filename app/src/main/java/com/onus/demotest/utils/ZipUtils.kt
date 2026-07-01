package com.onus.demotest.utils

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * @Author: onuszhao
 * @Date: 2024-05-29 11:24
 * @Description:
 */
object ZipUtils {

    fun unzip(zipFilePath: String?, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        val zipIn = ZipInputStream(FileInputStream(zipFilePath))
        var entry = zipIn.nextEntry
        while (entry != null) {
            val filePath = destDirectory + File.separator + entry.name
            Log.d("onuszhao", "entry=${entry.name}  isDirectory=${entry.isDirectory} filePath=${filePath}")
            if (!entry.isDirectory) {
                extractFile(zipIn, filePath)
            } else {
                val dir = File(filePath)
                dir.mkdir()
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        kotlin.runCatching {
            val buffer = ByteArray(4096)
            val fos = FileOutputStream(filePath)
            var bytesRead: Int
            while (zipIn.read(buffer).also { bytesRead = it } != -1) {
                fos.write(buffer, 0, bytesRead)
            }
            fos.close()
        }.onFailure {
            it.printStackTrace()
        }
    }
}