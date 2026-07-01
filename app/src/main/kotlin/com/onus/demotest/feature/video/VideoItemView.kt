package com.onus.demotest.feature.video

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView

/**
 * @Author: onuszhao
 * @Date: 2023-11-09 20:31
 * @Description:
 */
class VideoItemView(context: Context) : FrameLayout(context) {

    private val videoView = VideoView(context).apply {
        this@VideoItemView.addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private val mediaController = MediaController(context)

    private var isVideoPlaying = false

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        videoView.setOnCompletionListener {
            // 视频播放完成时的逻辑处理
            videoView.seekTo(0) // 将播放位置设置为起始位置
            videoView.start() // 开始重新播放视频
        }
    }

    fun bindData(data: VideoItemData) {
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)   // 设置媒体控制器
        videoView.setVideoURI(Uri.parse(data.path))          // 设置视频路径并开始播放
        videoView.start()
    }

    fun start() {
        Log.d("onuszhao", "VideoItemView  start  isVideoPlaying=$isVideoPlaying")
        videoView.seekTo(0) // 将播放位置设置为起始位置
        videoView.start()
    }

    fun stop() {
        Log.d("onuszhao", "VideoItemView  stop  isVideoPlaying=$isVideoPlaying")
        videoView.pause()
    }

    fun destroy(){
        videoView.stopPlayback()
    }
}