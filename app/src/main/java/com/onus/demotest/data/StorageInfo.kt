package com.onus.demotest.data

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.text.TextUtils
import java.io.File
import java.lang.reflect.Array
import java.lang.reflect.Method
import java.util.Objects

/**
 * Cloud View 2021 copyright.
 *
 * @Description: TODO
 * @User: tonysheng
 * @Date: 2022/5/13
 * @Time: 5:59 下午
 * @version: V1.0
 */
class StorageInfo(
    @JvmField
    var path: String? = null,
    @JvmField
    var mRemoveAble: Boolean = false,
    @JvmField
    var intent: Intent? = null,
) {
    companion object {
        var CACHE: List<StorageInfo>? = null

        @JvmStatic
        fun getAllStorage(context: Context, fromCache: Boolean): List<StorageInfo> {
            if (fromCache && CACHE != null) {
                return CACHE!!
            }

            val temp: HashMap<String, StorageInfo> = HashMap<String, StorageInfo>()
            val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val results =
                ArrayList<StorageInfo>()
            var storageVolumeClazz: Class<*>? = null
            try {
                storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
                val getVolumeList = mStorageManager.javaClass.getMethod("getVolumeList")
                val getPath: Method
                getPath = if (Build.VERSION.SDK_INT >= 30) {
                    storageVolumeClazz.getMethod("getDirectory")
                } else {
                    storageVolumeClazz.getMethod("getPath")
                }
                val isRemovable = storageVolumeClazz.getMethod("isRemovable")
                var createIntent: Method? = null
                try {
                    createIntent = storageVolumeClazz.getMethod("createAccessIntent", String::class.java)
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
                val result = getVolumeList.invoke(mStorageManager)
                val length = Array.getLength(result)
                for (i in 0 until length) {
                    val storageVolumeElement = Array.get(result, i)
                    val path = Objects.requireNonNull(getPath.invoke(storageVolumeElement)).toString()
                    val removable = Objects.requireNonNull(isRemovable.invoke(storageVolumeElement)) as Boolean
                    //有些设备读出来很多个存储但是却不能用所以这里要用mounted去判断一下
                    if (TextUtils.isEmpty(path) || Environment.MEDIA_MOUNTED != Environment.getStorageState(File(path))) {
                        continue
                    }
                    var intent: Intent? = null
                    if (Build.VERSION.SDK.toInt() >= 29) {
                        intent = invokeInstance(storageVolumeElement, "createOpenDocumentTreeIntent") as? Intent
                    } else if (createIntent != null) {
                        intent = createIntent.invoke(storageVolumeElement, null) as? Intent
                    } else {
                        intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                        //					intent.setPackage("com.android.documentsui");
                    }
                    val info: StorageInfo =
                        StorageInfo(path, removable, intent)
                    //google go的手机取出来这里是两个相同的path 一个removeable 一个不是,这里处理下抛弃掉remoable的
                    if (temp[path] != null) {
                        val oldInfo: StorageInfo = temp[path]!!
                        if (!info.mRemoveAble && oldInfo.mRemoveAble) {
                            results[results.indexOf(oldInfo)] = info
                            temp[path] = info
                        }
                        //					temp.put(path, info);
                    } else {
                        temp[path] = info
                        results.add(info)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            CACHE = results
            return results
        }

        private fun invokeInstance(obj: Any?, methodName: String): Any? {
            return if (obj == null) {
                null
            } else try {
                val cls: Class<*> = obj.javaClass
                val m = cls.getMethod(methodName)
                m.isAccessible = true
                m.invoke(obj)
            } catch (ignore: Throwable) {
                ignore.printStackTrace()
                null
            }
        }
    }
}