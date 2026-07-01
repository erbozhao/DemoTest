package com.onus.demotest.common

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import com.onus.demotest.data.FileInfo

/**
 * @Author: onuszhao
 * @Date: 2023-06-27 16:16
 * @Description:
 */
object CacheUtils {
    fun getUri(path: String): Uri {
        var tmpPath = path
        if (tmpPath.endsWith("/")) {
            tmpPath = tmpPath.substring(0, path.length - 1)
        }
        val replace = tmpPath.replace("/storage/emulated/0/", "").replace("/", "%2F")

        if (Build.VERSION.SDK_INT >= 33) {
            val dataIndex = "$tmpPath/".indexOf("Android/data/")
            val startIndex = if (dataIndex != -1) "Android/data/".length + dataIndex else 0
            var endIndex = "$tmpPath/".indexOf("/", startIndex)
            if (endIndex == -1) {
                endIndex = "$tmpPath/".length
            }
            val endPath = "$tmpPath/".substring(startIndex, endIndex)
            return Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2F$endPath/document/primary%3A$replace")
        }

        return if (path.contains("/Android/data")) {
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A$replace")
        } else {
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb/document/primary%3A$replace")
        }
    }

    /**
     * 获取data目录下所有文件夹uri
     */
    fun getAndroidDataUri(contentResolver: ContentResolver): Map<String, Uri> {
        val packageMap= mutableMapOf<String, Uri>()
        kotlin.runCatching {
            val uri = getUri("/storage/emulated/0/Android/data/")
            val androidDataUriTree = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri))
            val cursor = contentResolver.query(
                androidDataUriTree,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ),
                null,
                null,
                null
            )
            cursor?.let {
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                    val id = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
                    val type = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE))
                    if (type == DocumentsContract.Document.MIME_TYPE_DIR) {
                        packageMap[name] = DocumentsContract.buildChildDocumentsUriUsingTree(androidDataUriTree, id)
                    }
                }
            }
            cursor?.close()
        }.onFailure {
            LogUtils.log("getAndroidDataUri ${it.stackTraceToString()}")
        }
        return packageMap
    }

    /**
     * 获取URI下cache 目录，替换名称也可以获取其他目录
     */
    @SuppressLint("Range")
    fun getAndroidDataCacheUri(contentResolver: ContentResolver, uri: Uri): Uri? {
        var result: Uri? = null
        kotlin.runCatching {
            val cursor = contentResolver.query(uri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE),
                null, null, null)
            cursor?.let {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
                    val type = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE))
                    val name = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                    if (type == DocumentsContract.Document.MIME_TYPE_DIR && name.lowercase() == "cache") {
                        result = DocumentsContract.buildChildDocumentsUriUsingTree(uri, id)
                    }
                }
            }
            cursor?.close()
        }
        return result
    }

    fun getDataFromUri(contentResolver: ContentResolver, uri: Uri): List<FileInfo> {
        val result = mutableListOf<FileInfo>()
        kotlin.runCatching {
            val cursor = contentResolver.query(
                uri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE
                ),
                null,
                null,
                null
            )
            cursor?.let {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
                    val type = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE))
                    val name = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                    val time = it.getLong(it.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED))
                    val size = it.getLong(it.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE))
                    val pathSegments = uri.path?.split(":")
                    val path = if (pathSegments.isNullOrEmpty()) uri.toString() else pathSegments[pathSegments.size - 1].replace("/children", "")
                    if (type == DocumentsContract.Document.MIME_TYPE_DIR){
                        result.add(FileInfo(true, "$path/$name", name, time, size))

                        val chirldUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, id)
                        val childResult = getDataFromUri(contentResolver, chirldUri)
                        result.addAll(childResult)
                    }else {
                        result.add(FileInfo(false, "$path/$name", name, time, size))
                    }
                }
            }
            cursor?.close()
        }.onFailure {
            LogUtils.log("getAndroidDataUri ${it.stackTraceToString()}")
        }
        return result
    }

}