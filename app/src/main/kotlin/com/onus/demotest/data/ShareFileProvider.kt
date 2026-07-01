package com.onus.demotest.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.webkit.MimeTypeMap
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Created by burtonsun on 2016/8/29.
 */
class ShareFileProvider : ContentProvider() {
    private var mStrategy: PathStrategy? = null
    private var mInfo: ProviderInfo? = null
    private var hasInit = false
    override fun onCreate(): Boolean {
        return true
    }

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)
        mInfo = if (info.exported) {
            throw SecurityException("Provider must not be exported")
        } else if (!info.grantUriPermissions) {
            throw SecurityException("Provider must grant uri permissions")
        } else {
            info
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var projection = projection
        initPathStrategyIfNeed()
        if (mStrategy == null) {
            return null
        }
        val file = mStrategy!!.getFileForUri(uri)
        if (projection == null) {
            projection = COLUMNS
        }
        var cols = arrayOfNulls<String>(projection.size)
        var values = arrayOfNulls<Any>(projection.size)
        var i = 0
        val cursor = projection
        val `len$` = projection.size
        for (`i$` in 0 until `len$`) {
            val col = cursor[`i$`]
            if ("_display_name" == col) {
                cols[i] = "_display_name"
                values[i++] = file!!.name
            } else if ("_size" == col) {
                cols[i] = "_size"
                values[i++] = java.lang.Long.valueOf(file!!.length())
            } else if ("_data" == col) {
                cols[i] = "_data"
                values[i++] = file!!.absolutePath
            }
        }
        cols = copyOf(cols, i)
        values = copyOf(values, i)
        val var14 = MatrixCursor(cols, 1)
        var14.addRow(values)
        return var14
    }

    override fun getType(uri: Uri): String? {
        initPathStrategyIfNeed()
        if (mStrategy == null) {
            return ""
        }
        val file = mStrategy!!.getFileForUri(uri)
        val lastDot = file!!.name.lastIndexOf(46.toChar())
        if (lastDot >= 0) {
            val extension = file.name.substring(lastDot + 1)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mime != null) {
                return mime
            }
        }
        return "application/octet-stream"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("No external inserts")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("No external updates")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        initPathStrategyIfNeed()
        if (mStrategy == null) {
            return 0
        }
        val file = mStrategy!!.getFileForUri(uri)
        return if (file!!.delete()) 1 else 0
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        initPathStrategyIfNeed()
        if (mStrategy == null) {
            return null
        }
        val file = mStrategy!!.getFileForUri(uri)
        val fileMode = modeToMode(mode)
        return ParcelFileDescriptor.open(file, fileMode)
    }

    internal class SimplePathStrategy(private val mAuthority: String) : PathStrategy {
        private val mRoots  = HashMap<String?, File?>()
        fun addRoot(name: String?, root: File) {
            var root = root
            require(!TextUtils.isEmpty(name)) { "Name must not be empty" }
            root = try {
                root.canonicalFile
            } catch (var4: IOException) {
                throw IllegalArgumentException("Failed to resolve canonical path for $root", var4)
            }
            mRoots[name] = root
        }

        override fun getUriForFile(file: File): Uri? {
            var path: String
            path = try {
                file.canonicalPath
            } catch (var7: IOException) {
                throw IllegalArgumentException("Failed to resolve canonical path for $file")
            }
            var mostSpecific: Map.Entry<*, *>? = null
            val rootPath: Iterator<*> = mRoots.entries.iterator()
            while (true) {
                var root: Map.Entry<*, *>
                var rootPath1: String
                do {
                    do {
                        if (!rootPath.hasNext()) {
                            requireNotNull(mostSpecific) { "Failed to find configured root that contains $path" }
                            val rootPath2 = (mostSpecific.value as File).path
                            path = if (rootPath2.endsWith("/")) {
                                path.substring(rootPath2.length)
                            } else {
                                path.substring(rootPath2.length + 1)
                            }
                            path = Uri.encode(mostSpecific.key as String?) + '/' + Uri.encode(path, "/")
                            return Uri.Builder().scheme("content").authority(mAuthority).encodedPath(path).build()
                        }
                        root = rootPath.next() as Map.Entry<*, *>
                        rootPath1 = (root.value as File).path
                    } while (!path.startsWith(rootPath1))
                } while (mostSpecific != null && rootPath1.length <= (mostSpecific.value as File).path.length)
                mostSpecific = root
            }
        }

        override fun getFileForUri(uri: Uri): File? {
            var path = uri.encodedPath
            if (TextUtils.isEmpty(path)) return null
            val splitIndex = path!!.indexOf(47.toChar(), 1)
            val tag = Uri.decode(path.substring(1, splitIndex))
            path = Uri.decode(path.substring(splitIndex + 1))
            val root = mRoots[tag]
            return if (root == null) {
                throw IllegalArgumentException("Unable to find configured root for $uri")
            } else {
                var file = File(root, path)
                file = try {
                    file.canonicalFile
                } catch (var8: IOException) {
                    throw IllegalArgumentException("Failed to resolve canonical path for $file")
                }
                if (!file.path.startsWith(root.path)) {
                    throw SecurityException("Resolved path jumped beyond configured root")
                } else {
                    file
                }
            }
        }
    }

    @Synchronized
    private fun initPathStrategyIfNeed() {
        try {
            if (!hasInit && mStrategy == null && mInfo != null) {
                mStrategy = getPathStrategy(context, mInfo!!.authority)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        hasInit = true
    }

    internal interface PathStrategy {
        fun getUriForFile(var1: File): Uri?
        fun getFileForUri(var1: Uri): File?
    }

    companion object {
        private val COLUMNS = arrayOf("_display_name", "_size", "_data")
        private val DEVICE_ROOT = File("/")
        private val sCache  = HashMap<String?, PathStrategy?>()
        fun getUriForFile(context: Context, file: File): Uri? {
            val strategy = getPathStrategy(context, "${context.packageName}.sharefileprovider")
            return strategy!!.getUriForFile(file)
        }

        fun fromFile(context: Context, file: File): Uri? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    val strategy = getPathStrategy(context, "${context.packageName}.sharefileprovider")
                    return strategy!!.getUriForFile(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return Uri.fromFile(file)
        }

        private fun getPathStrategy(context: Context?, authority: String): PathStrategy? {
            val var3: HashMap<*, *> = sCache
            synchronized(sCache) {
                var strat = sCache[authority]
                if (strat == null) {
                    strat = try {
                        parsePathStrategy(context, authority)
                    } catch (var6: IOException) {
                        throw IllegalArgumentException("Failed to parse android.support.FILE_PROVIDER_PATHS meta-data", var6)
                    } catch (var7: XmlPullParserException) {
                        throw IllegalArgumentException("Failed to parse android.support.FILE_PROVIDER_PATHS meta-data", var7)
                    }
                    sCache[authority] = strat
                }
                return strat
            }
        }

        @Throws(IOException::class, XmlPullParserException::class)
        private fun parsePathStrategy(context: Context?, authority: String): PathStrategy {
            val strat = SimplePathStrategy(authority)
            val info = context!!.packageManager.resolveContentProvider(authority, 128)
                ?: throw IllegalArgumentException("Missing android.support.FILE_PROVIDER_PATHS meta-data")
            val `in` = info.loadXmlMetaData(context.packageManager, "android.support.FILE_PROVIDER_PATHS")
            return if (`in` == null) {
                throw IllegalArgumentException("Missing android.support.FILE_PROVIDER_PATHS meta-data")
            } else {
                var type: Int
                while (`in`.next().also { type = it } != 1) {
                    if (type == 2) {
                        val tag = `in`.name
                        val name = `in`.getAttributeValue(null as String?, "name")
                        val path = `in`.getAttributeValue(null as String?, "path")
                        var target: File? = null
                        if ("root-path" == tag) {
                            target = buildPath(DEVICE_ROOT, *arrayOf(path))
                        } else if ("files-path" == tag) {
                            target = buildPath(context.filesDir, *arrayOf(path))
                        } else if ("cache-path" == tag) {
                            target = buildPath(context.cacheDir, *arrayOf(path))
                        } /*else if("external-path".equals(tag)) {
                        target = buildPath(FileUtils.getExternalStorageDirectory(), new String[]{path});
                    }*/
                        if (target != null) {
                            strat.addRoot(name, target)
                        }
                    }
                }
                `in`.close()
                strat
            }
        }

        private fun modeToMode(mode: String): Int {
            val modeBits: Int
            modeBits = if ("r" == mode) {
                268435456
            } else if ("w" != mode && "wt" != mode) {
                if ("wa" == mode) {
                    704643072
                } else if ("rw" == mode) {
                    939524096
                } else {
                    require("rwt" == mode) { "Invalid mode: $mode" }
                    1006632960
                }
            } else {
                738197504
            }
            return modeBits
        }

        private fun buildPath(base: File, vararg segments: String): File {
            var cur = base
            val arr = segments
            val len = segments.size
            for (i in 0 until len) {
                val segment = arr[i]
                if (segment != null) {
                    cur = File(cur, segment)
                }
            }
            return cur
        }

        private fun copyOf(original: Array<String?>, newLength: Int): Array<String?> {
            val result = arrayOfNulls<String>(newLength)
            System.arraycopy(original, 0, result, 0, newLength)
            return result
        }

        private fun copyOf(original: Array<Any?>, newLength: Int): Array<Any?> {
            val result = arrayOfNulls<Any>(newLength)
            System.arraycopy(original, 0, result, 0, newLength)
            return result
        }
    }
}