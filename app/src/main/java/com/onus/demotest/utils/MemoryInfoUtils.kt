package com.onus.demotest.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.util.regex.Pattern

object MemoryInfoUtils {

    private var getMemoryStatMethod: Method? = null

    fun getMemoryStat(statName: String?, memoryInfo: Debug.MemoryInfo?): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return -1
        }
        try {
            if (getMemoryStatMethod == null) {
                val ownerClass = Class.forName("android.os.Debug\$MemoryInfo")
                val argsClass: Array<Class<*>> = arrayOf()
                argsClass[0] = String::class.java
                getMemoryStatMethod = ownerClass.getMethod("getMemoryStat", *argsClass)
            }
            val params = arrayOfNulls<Any>(1)
            params[0] = statName
            return (getMemoryStatMethod!!.invoke(memoryInfo, *params) as String).toInt()
        } catch (e: Exception) {
            Log.e("MemoryInfoUtil", e.toString())
        }
        return -1
    }

    fun getTotalUss(memoryInfo: Debug.MemoryInfo): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return -1
        }
        return memoryInfo.dalvikPrivateDirty + memoryInfo.nativePrivateDirty + memoryInfo.otherPrivateDirty + memoryInfo.totalPrivateClean
    }

    /**
     * 某些机型上物理内存值固定不变，暂未解决办法
     *
     * @param context 上下文
     * @param pid 父进程id
     * @return 内存信息数组
     */
    fun getPss(context: Context, pid: Int): LongArray {
        val value = LongArray(3) // Natvie Dalvik Total
        if (pid >= 0) {
            val pids = IntArray(1)
            pids[0] = pid
            val mAm: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfoArray: Array<Debug.MemoryInfo> = mAm.getProcessMemoryInfo(pids)
            val pidMemoryInfo = memoryInfoArray[0]
            value[0] = pidMemoryInfo.nativePss.toLong()
            value[1] = pidMemoryInfo.dalvikPss.toLong()
            value[2] = pidMemoryInfo.totalPss.toLong()
        }
        return value
    }

    /**
     * The total memory accessible by the kernel. This is basically the RAM size of the device,
     * not including below-kernel fixed allocations like DMA buffers, RAM for the baseband CPU, etc.
     * @return device RAM size or -1
     */
    fun getDeviceRAMSize(context: Context): Long {
        try {
            // Declaring and Initializing the ActivityManager
            val manager: ActivityManager = getActivityManager(context)
            // Declaring MemoryInfo object
            val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
            // Fetching the data from the ActivityManager
            manager.getMemoryInfo(memInfo)
            return memInfo.totalMem
        } catch (e: Exception) {
            // ignore
            return -1
        }
    }

    fun getVmSize(pid: Int): Long {
        val status = String.format("/proc/%s/status", pid)
        try {
            val content = getStringFromFile(status).trim { it <= ' ' }
            val args = content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (str in args) {
                if (str.startsWith("VmSize")) {
                    val p = Pattern.compile("\\d+")
                    val matcher = p.matcher(str)
                    if (matcher.find()) {
                        return matcher.group().toLong()
                    }
                }
            }
            if (args.size > 12) {
                val p = Pattern.compile("\\d+")
                val matcher = p.matcher(args[12])
                if (matcher.find()) {
                    return matcher.group().toLong()
                }
            }
        } catch (e: Exception) {
            return -1
        }
        return -1
    }

    @Throws(Exception::class)
    fun getStringFromFile(filePath: String?): String {
        val fl = File(filePath)
        var fin: FileInputStream? = null
        val ret: String
        try {
            fin = FileInputStream(fl)
            ret = convertStreamToString(fin)
        } finally {
            fin?.close()
        }
        return ret
    }

    @Throws(Exception::class)
    fun convertStreamToString(`is`: InputStream?): String {
        var reader: BufferedReader? = null
        val sb = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(`is`))
            var line: String? = null
            while ((reader.readLine().also { line = it }) != null) {
                sb.append(line).append('\n')
            }
        } finally {
            reader?.close()
        }
        return sb.toString()
    }

    fun getMemDetailInfo(context: Context, pid: Int): String {
        val rawMemoryInfo = getAppMemInfo(context, pid) ?: return ""
        val vm = getVmSize(pid)
        val memoryInfo = getMemDetailInfo(rawMemoryInfo, vm)
        return memoryInfo + javaMemoInfo
    }

    private val javaMemoInfo: String
        get() {
            // 增加Java内存使用占比，超过85%则输出当前Page路径
            val runtime = Runtime.getRuntime()
            val javaMax = runtime.maxMemory()
            val javaTotal = runtime.totalMemory()
            val javaFree = runtime.freeMemory()
            val javaUsed = javaTotal - javaFree
            // Java 内存使用超过最大限制的 85%
            val proportion = javaUsed.toFloat() / javaMax * 100
            val builder = StringBuilder()
            builder.append("\nJava Memo Usage: ").append(Math.round(proportion)).append("%\n")
                .append("maxMemory: ").append(javaMax / 1024 / 1024).append("M\n")
                .append("totalMemory: ").append(javaTotal / 1024 / 1024).append("M\n")
                .append("freeMemory: ").append(javaFree / 1024 / 1024).append("M")
            return builder.toString()
        }

    /**
     * 传入1024 返回1
     */
    fun kbStringToM(kb: String): String {
        try {
            if (TextUtils.isEmpty(kb)) {
                return "-M"
            }
            val ikb = kb.toInt()
            return (ikb / 1024).toString() + "M"
        } catch (e: Throwable) {
            return "-M"
        }
    }

    fun byteStringToM(b: Long): String {
        return try {
            (b / 1024 / 1024).toString() + "M"
        } catch (e: Throwable) {
            "-M"
        }
    }

    fun getAppMemInfo(context: Context?, fPid: Int): Debug.MemoryInfo? {
        try {
            var memInfo: Debug.MemoryInfo? = null
            if (context == null) {
                return null
            }
            //28 为Android P
            if (Build.VERSION.SDK_INT > 28 && Looper.getMainLooper() == Looper.myLooper()) {
                memInfo = Debug.MemoryInfo()
                Debug.getMemoryInfo(memInfo)
                return memInfo
            } else {
                val am: ActivityManager = getActivityManager(context) ?: return null
                val memInfos: Array<Debug.MemoryInfo> = am.getProcessMemoryInfo(intArrayOf(fPid))
                if (null != memInfos && memInfos.size > 0) {
                    return memInfos[0]
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    private fun getActivityManager(context: Context): ActivityManager {
        return context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    fun getMemDetailInfo(rawMemoryInfo: Debug.MemoryInfo, vm: Long): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("vm-size:")
        val vmSize = byteStringToM(vm)
        stringBuilder.append(vmSize).append("M").append("\n")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stringBuilder.append("total-pss:")
            stringBuilder.append(kbStringToM(rawMemoryInfo.getMemoryStat("summary.total-pss"))).append("\n")
            stringBuilder.append("java-heap:")
            stringBuilder.append(kbStringToM(rawMemoryInfo.getMemoryStat("summary.java-heap"))).append("\n")
            stringBuilder.append("native-heap:")
            stringBuilder.append(kbStringToM(rawMemoryInfo.getMemoryStat("summary.native-heap"))).append("\n")
            stringBuilder.append("code:")
            stringBuilder.append(kbStringToM(rawMemoryInfo.getMemoryStat("summary.code"))).append("\n")
            stringBuilder.append("graphics:")
            stringBuilder.append(kbStringToM(rawMemoryInfo.getMemoryStat("summary.graphics"))).append("\n")
            stringBuilder.append("private-other:")
            stringBuilder.append(kbStringToM(rawMemoryInfo.getMemoryStat("summary.private-other"))).append("\n")
        }
        return stringBuilder.toString()
    }

    fun getMemDetailInfo(pss: String, java: String, code: String, natives: String, graphics: String, privateOther: String, vm: Long): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("vm-size:")
        val vmSize = byteStringToM(vm)
        stringBuilder.append(vmSize).append("M").append("\n")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stringBuilder.append("total-pss:")
            stringBuilder.append(kbStringToM(pss)).append("\n")
            stringBuilder.append("java-heap:")
            stringBuilder.append(kbStringToM(java)).append("\n")
            stringBuilder.append("native-heap:")
            stringBuilder.append(kbStringToM(natives)).append("\n")
            stringBuilder.append("code:")
            stringBuilder.append(kbStringToM(code)).append("\n")
            stringBuilder.append("graphics:")
            stringBuilder.append(kbStringToM(graphics)).append("\n")
            stringBuilder.append("private-other:")
            stringBuilder.append(kbStringToM(privateOther)).append("\n")
        }
        return stringBuilder.toString()
    }
}
