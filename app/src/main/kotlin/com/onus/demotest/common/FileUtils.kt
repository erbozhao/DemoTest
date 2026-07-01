package com.onus.demotest.common

import android.os.Environment
import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileFilter

/**
 * @Author: onuszhao
 * @Date: 2023-06-27 18:06
 * @Description:
 */
object FileUtils {

    fun getFileExtension(fileName: String): String? {
        if (TextUtils.isEmpty(fileName)) {
            return null
        }

        var fileExt: String? = null
        val idx = fileName.lastIndexOf(".")
        if (idx > -1 && idx < fileName.length - 1) {
            fileExt = fileName.substring(idx + 1);
            if (fileExt.indexOf(File.separatorChar) > -1) {
                return null;
            }
        }
        return fileExt;
    }

    fun getMimeTypeFromExtension(ext: String?, def: String): String {
        var mimeType = def
        if (!TextUtils.isEmpty(ext)) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: ""
            if (TextUtils.isEmpty(mimeType)) {
                mimeType = def
            }
        }
        return mimeType
    }

    fun getUnknownMimeType(fileExt: String?) = when (fileExt) {
        "chm" -> "text/plain"
        "epub" -> "application/epub"
        else -> "*/*"
    }

    fun getExternalStorageDirectory(): File {
        return try {
            Environment.getExternalStorageDirectory()
        } catch (throwable: Throwable) {
            File("/storage/emulated/0")
        }
    }

    fun getFolderSize(file: File?): Long {
        var size = 0L
        kotlin.runCatching {
            file?.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getFolderSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    fun deleteFile(dir: File?) {
        dir?.let {
            if (it.isDirectory) {
                it.listFiles()?.forEach { childFile ->
                    deleteFile(childFile)
                }
            } else {
                it.delete()
            }
        }
    }

    /**
     * 遍历该目录下的所有文件
     */
    fun listFileRecursive(dir: String): List<File> {
        val popList = mutableListOf(File(dir))
        val result = mutableListOf<File>()
        while (true) {
            val fileDir = popList.removeFirstOrNull() ?: break
            fileDir.listFiles(FileFilter {
                !it.isHidden
            })?.forEach {
                if (it.isDirectory) {
                    popList.add(it)
                } else {
                    result.add(it)
                }
            }
        }
        return result
    }
}