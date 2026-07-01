package com.onus.demotest.core.const

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.onus.demotest.common.CommonUtils
import com.onus.demotest.common.FileUtils
import java.io.File

/**
 * Cloud View 2021 copyright.
 *
 * @Description: TODO
 * @User: tonysheng
 * @Date: 2022/6/2
 * @Time: 2:47 下午
 * @version:
 */
object WhatsAppFileConst {

    private val STORAGE_PATH = FileUtils.getExternalStorageDirectory().absolutePath
    private val WHATSAPP_PATH = STORAGE_PATH + File.separator + "WhatsApp"
    private val WHATSAPP_BUSINESS_PATH = STORAGE_PATH + File.separator + "WhatsApp Business"
    private val GB_WHATSAPP_PATH = STORAGE_PATH + File.separator + "GBWhatsApp"

    private val WHATSAPP_PATH_NEW = STORAGE_PATH + File.separator + "Android/media/com.whatsapp/WhatsApp"
    private val WHATSAPP_BUSINESS_PATH_NEW = STORAGE_PATH + File.separator + "Android/media/com.whatsapp.w4b/WhatsApp Business"
    private val GB_WHATSAPP_PATH_NEW = STORAGE_PATH + File.separator + "Android/media/com.gbwhatsapp/GBWhatsApp"

    val statusPaths = mutableListOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + ".Statuses",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + ".Statuses",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + ".Statuses",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + ".Statuses",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + ".Statuses",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + ".Statuses"
    )

    val imagePaths = mutableListOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Images",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Images",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Images",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Images",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Images",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Images"
    )

    val videoPaths = mutableListOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Video",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Video",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Video",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Video",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Video",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Video"
    )

    val stickerPaths = mutableListOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Stickers",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Stickers",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Stickers",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Stickers",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Stickers",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Stickers"
    )

    private val docPaths = listOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Documents",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Documents",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Documents",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Documents",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Documents",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Documents"
    )
    private val audioPaths = listOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Audio",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Audio",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Audio",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Audio",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Audio",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Audio"
    )
    private val gifPaths = listOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Animated Gifs",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Animated Gifs",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Animated Gifs",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Animated Gifs",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Animated Gifs",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Animated Gifs"
    )

    private val profilePaths = listOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Profile Photos",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Profile Photos",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Profile Photos",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Profile Photos",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Profile Photos",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Profile Photos"
    )
    private val wallPaperPaths = listOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp WallPaper",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business WallPaper",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp WallPaper",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp WallPaper",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business WallPaper",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp WallPaper"
    )
    private val voiceNotePaths = listOf(
        WHATSAPP_PATH + File.separator + "Media" + File.separator + "WhatsApp Voice Notes",
        WHATSAPP_BUSINESS_PATH + File.separator + "Media" + File.separator + "WhatsApp Business Voice Notes",
        GB_WHATSAPP_PATH + File.separator + "Media" + File.separator + "GBWhatsApp Voice Notes",
        WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Voice Notes",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media" + File.separator + "WhatsApp Business Voice Notes",
        GB_WHATSAPP_PATH_NEW + File.separator + "Media" + File.separator + "GBWhatsApp Voice Notes"
    )
    private val otherPaths = listOf(
        WHATSAPP_PATH + File.separator + "Media", WHATSAPP_BUSINESS_PATH + File.separator + "Media",
        GB_WHATSAPP_PATH + File.separator + "Media", WHATSAPP_PATH_NEW + File.separator + "Media",
        WHATSAPP_BUSINESS_PATH_NEW + File.separator + "Media", GB_WHATSAPP_PATH_NEW + File.separator + "Media"
    )

    fun openWhatsApp(context: Context) {
        val activities = context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN), 0)
        val whatsAppPkgs = mutableListOf<Pair<String, String>>().apply {
            add(Pair("com.whatsapp.w4b", "com.whatsapp.Main"))
            add(Pair("com.whatsapp", "com.whatsapp.Main"))
            add(Pair("com.gbwhatsapp", "com.gbwhatsapp.Main"))
        }

        var openSuccessed = false
        for (i in whatsAppPkgs.indices) {
            val pkgName = whatsAppPkgs[i].first
            val className = whatsAppPkgs[i].second
            if (CommonUtils.isPkgInstalled(pkgName, context)) {
                kotlin.runCatching {
                    val intent = Intent()
                    intent.setClassName(pkgName, className)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    openSuccessed = true
                }.onFailure {
                    it.printStackTrace()
                    openSuccessed = false
                }
            } else {
                openSuccessed = false
            }
            if (openSuccessed) {
                break
            }
        }

        if (!openSuccessed) {
            Toast.makeText(context, "请安装WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    const val sticker_details_path = "sticker_path"
}