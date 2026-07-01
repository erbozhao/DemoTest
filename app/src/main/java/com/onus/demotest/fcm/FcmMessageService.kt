package com.onus.demotest.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onus.demotest.utils.CommonUtils

/**
 * @Author: onuszhao
 * @Date: 2023-11-22 21:12
 * @Description:
 */
class FcmMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("onuszhao", "onNewToken  token=$token")
        // EntranceBackgroundWorker.post {
        //     PushManager.getInstance().handleFcmToken(token)
        // }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.d("onuszhao", "onMessageReceived  message=$p0  cur=${Thread.currentThread()}")
        // EntranceBackgroundWorker.post {
        //     PushManager.getInstance().handleFcmMessage(p0)
        // }

        CommonUtils.hasBoot = true
    }
}