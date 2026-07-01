package com.onus.demotest.services

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log

/**
 * @Author: onuszhao
 * @Date: 2024-04-15 20:31
 * @Description:
 */
class IPCServiceProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        Log.d("onuszhao", "IPCServiceProvider  onCreate")
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        Log.d("onuszhao", "IPCServiceProvider  call")
        return super.call(method, arg, extras)
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }
}