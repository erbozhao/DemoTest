package com.onus.demotest.feature.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * @author onuszhao
 * @since 2025/12/4
 * @description
 */

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("onuszhao", "MyBroadcastReceiver  onReceive  intent=${intent?.action}")
    }
}