package com.onus.demotest.feature.notification

import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationBuilderWithBuilderAccessor
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.onus.demotest.R

/**
 * @author onuszhao
 * @since 2026/1/9
 * @description
 */

class MyCustomMessagingStyle(val context: Context, user: Person) : NotificationCompat.MessagingStyle(user) {

    override fun makeContentView(builder: NotificationBuilderWithBuilderAccessor?): RemoteViews? {
        val remoteView = RemoteViews(context.packageName, R.layout.custom_notification_fold)
        val lastMessage = messages.lastOrNull()
        if (lastMessage != null) {
            remoteView.setTextViewText(R.id.title, lastMessage.person?.name)
            remoteView.setTextViewText(R.id.desc, lastMessage.text)
        }
        return remoteView
    }

    override fun makeBigContentView(builder: NotificationBuilderWithBuilderAccessor?): RemoteViews? {
        val remoteView = RemoteViews(context.packageName, R.layout.custom_notification_expand)
        val lastMessage = messages.lastOrNull()
        if (lastMessage != null) {
            remoteView.setTextViewText(R.id.title, lastMessage.person?.name)
            remoteView.setTextViewText(R.id.desc, lastMessage.text)
        }
        return remoteView
    }

    override fun makeHeadsUpContentView(builder: NotificationBuilderWithBuilderAccessor?): RemoteViews? {
        val remoteView = RemoteViews(context.packageName, R.layout.custom_notification_headsup)
        val lastMessage = messages.lastOrNull()
        if (lastMessage != null) {
            remoteView.setTextViewText(R.id.title, lastMessage.person?.name)
            remoteView.setTextViewText(R.id.desc, lastMessage.text)
        }
        return remoteView
    }
}