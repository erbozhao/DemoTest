package com.onus.demotest.pages.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadata
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.onus.demotest.R

/**
 * @author onuszhao
 * @since 2025/12/5
 * @description
 */

@OptIn(UnstableApi::class)
class MediaPlaybackServiceCompat : MediaBrowserServiceCompat() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private val mediaCallback = MediaSessionCallback()
    private val playerListener = PlayerEventListener()

    companion object {
        const val CHANNEL_ID = "MediaPlaybackChannel"
        val NOTIFICATION_ID = NotifyDemoActivity.Companion.generateNotifyId()

        // 根 ID 用于 MediaBrowserService
        const val SERVICE_ROOT_ID = "media_root_id"

        const val ACTION_CLOSE = "com.onus.demotest.ACTION_CLOSE"
        const val ACTION_FAVORITE = "com.onus.demotest.ACTION_FAVORITE"

        const val CLOSE_BTN = 104
        const val FAV_BTN = 105
    }

    override fun onCreate() {
        super.onCreate()

        // 1. 初始化 ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(playerListener)

        // 2. 初始化 MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "MediaPlaybackServiceTag")

        // 3. 设置 PlaybackStateCompat Builder
        stateBuilder = PlaybackStateCompat.Builder()
            .setActions(getPlaybackActions())

        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.setCallback(mediaCallback)

        // 4. 将 Session Token 设置给 Service，允许 MediaBrowser 连接
        sessionToken = mediaSession.sessionToken

        // 5. 设置媒体内容
        val mediaItem = MediaItem.Builder()
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3") // **请替换为您的实际音频URI**
            .setMediaMetadata(createSampleMediaMetadata())
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play() // 添加这一行开始播放

        // 6. 激活 Media Session
        mediaSession.isActive = true

        // 7. 首次启动前台服务
        updateNotification()
    }

    /**
     * 定义 MediaSessionCompat 支持的动作 (Play, Pause, Skip, etc.)
     */
    private fun getPlaybackActions(): Long {
        return PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_SEEK_TO
        // PlaybackStateCompat.ACTION_PLAY_PAUSE or
        // PlaybackStateCompat.ACTION_FAST_FORWARD or
        // PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
        // PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
        // PlaybackStateCompat.ACTION_PLAY_FROM_URI or
        // PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
        // PlaybackStateCompat.ACTION_SET_RATING or
        // PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
        // PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE or
        // PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED or
        // PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED or
        // PlaybackStateCompat.ACTION_PREPARE or
        // PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
        // PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
        // PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
        // PlaybackStateCompat.ACTION_REWIND or
        // PlaybackStateCompat.ACTION_STOP
    }

    // --- MediaBrowserServiceCompat 抽象方法实现 ---

    /**
     * 允许客户端连接到 MediaBrowserService。
     */
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        // 允许所有应用连接 (实际应用中应限制包名)
        return BrowserRoot(SERVICE_ROOT_ID, null)
    }

    /**
     * 当客户端请求媒体内容时调用。
     */
    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // 简单的 Demo 不提供媒体库浏览，直接返回空列表
        result.sendResult(null)
    }

    // --- MediaSessionCompat.Callback (处理来自通知或控制器的命令) ---

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            if (exoPlayer.playbackState == Player.STATE_READY) {
                exoPlayer.play()
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }
        }

        override fun onPause() {
            exoPlayer.pause()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }

        override fun onSkipToNext() {
            exoPlayer.seekToNextMediaItem()
        }

        override fun onSkipToPrevious() {
            exoPlayer.seekToPreviousMediaItem()
        }

        override fun onStop() {
            exoPlayer.stop()
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
            stopSelf()
        }
    }

    // --- ExoPlayer 状态监听器 ---

    private inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    updatePlaybackState(
                        if (exoPlayer.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                    )
                }

                Player.STATE_ENDED -> {
                    updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
                    stopSelf()
                }

                Player.STATE_BUFFERING -> updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                Player.STATE_IDLE -> updatePlaybackState(PlaybackStateCompat.STATE_NONE)
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
            // 更新 MediaSessionCompat 的元数据
            mediaSession.setMetadata(buildMediaMetadataCompat(exoPlayer))
        }
    }

    /**
     * 更新 MediaSessionCompat 的 PlaybackState 并刷新通知。
     */
    private fun updatePlaybackState(state: Int) {
        stateBuilder.setState(
            state,
            exoPlayer.currentPosition,
            1.0f // 播放速度
        )
        mediaSession.setPlaybackState(stateBuilder.build())
        updateNotification()
    }

    // --- 通知和元数据构建辅助方法 ---

    private fun buildMediaMetadataCompat(player: ExoPlayer): MediaMetadataCompat {
        val mediaMetadata = player.mediaMetadata
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaMetadata.title?.toString() ?: "Unknown Title")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mediaMetadata.artist?.toString() ?: "Unknown Artist")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.duration)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Test Album")
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(resources, R.drawable.music_cover_bg))
            .build()
    }

    private fun createSampleMediaMetadata(): androidx.media3.common.MediaMetadata {
        return androidx.media3.common.MediaMetadata.Builder()
            .setTitle("Compat Package Song Compat Package Song Compat Package Song Compat Package Song")
            .setArtist("The Gemini Assistant The Gemini Assistant The Gemini Assistant The Gemini Assistant")
            .setIsPlayable(true)
            .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC)
            .setDurationMs(180_000L)
            .setAlbumTitle("Album Title")
            .setAlbumArtist("Album")
            .setArtworkUri("https://www.gemini.com/wp-content/uploads/2020/09/gemini-logo-white.png".toUri())
            .setSubtitle("Subtitle")
            .build()
    }

    /**
     * 构建并发送通知，同时处理前台服务。
     */
    private fun updateNotification() {
        val playbackState = mediaSession.controller.playbackState ?: return
        val metadata = mediaSession.controller.metadata ?: return

        createNotificationChannel()

        val isPlaying = playbackState.state == PlaybackStateCompat.STATE_PLAYING
        val actionIcon = if (isPlaying) R.drawable.music_pause else R.drawable.music_play
        val actionCode = if (isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    // 使用 MediaSessionCompat 的 Token
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2, 3, 4)
                    .setShowCancelButton(true)
            )
            .setContentTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
            .setContentText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.music_album))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // .setAutoCancel(true)
            // .setColor(Color.RED)
        notificationBuilder.setGroup("Group_Media")

        // 0: 上一曲
        notificationBuilder.addAction(
            R.drawable.music_previous, "Previous",
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        )
        // 1: 播放/暂停
        notificationBuilder.addAction(
            actionIcon, "Play/Pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, actionCode)
        )
        // 2: 下一曲
        notificationBuilder.addAction(
            R.drawable.music_next, "Next",
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        )
        // 3: 收藏
        notificationBuilder.addAction(R.drawable.music_fav, "Fav", getPendingIntent(this, ACTION_FAVORITE, FAV_BTN))
        // 4: 关闭
        notificationBuilder.addAction(R.drawable.music_close, "Close", getPendingIntent(this, ACTION_CLOSE, CLOSE_BTN))

        val notification = notificationBuilder.build()

        // 启动/更新前台服务
        if (playbackState.state == PlaybackStateCompat.STATE_PLAYING || playbackState.state == PlaybackStateCompat.STATE_BUFFERING) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            // 在 Android O+ 停止播放时，保持通知但移除前台状态
            stopForeground(false)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    // 辅助方法：创建通知渠道
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val name = "音乐播放"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onDestroy() {
        mediaSession.isActive = false
        mediaSession.release()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        stopForeground(true)
        super.onDestroy()
    }

    // 处理 Media Button Intent，确保 MediaSessionCompat 接收命令
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_FAVORITE -> {
                    // 处理收藏逻辑
                }

                ACTION_CLOSE -> {
                    // 停止播放并关闭服务
                    exoPlayer.stop()
                    stopSelf()
                    return START_NOT_STICKY
                }

                else -> {
                    // 处理媒体按钮事件
                    MediaButtonReceiver.handleIntent(mediaSession, intent)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent()
        intent.action = action
        intent.setPackage(context.packageName)
        intent.component = ComponentName(context, MediaPlaybackServiceCompat::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(
                context,
                requestCode,
                intent,
                getMutableUpdateFlag()
            )
        } else {
            return PendingIntent.getService(
                context,
                requestCode,
                intent,
                getMutableUpdateFlag()
            )
        }
    }

    private fun getMutableUpdateFlag(): Int {
        var flag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= 31) {
            flag = flag or PendingIntent.FLAG_MUTABLE
        }
        return flag
    }


    private fun getImmutableUpdateFlag(): Int {
        var flag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= 31) {
            flag = flag or PendingIntent.FLAG_IMMUTABLE
        }
        return flag
    }
}