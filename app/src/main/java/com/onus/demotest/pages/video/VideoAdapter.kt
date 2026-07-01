package com.onus.demotest.pages.video

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author: onuszhao
 * @Date: 2023-11-09 20:30
 * @Description:
 */
class VideoAdapter : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val items = mutableListOf<VideoItemData>()

    private val views = mutableListOf<VideoItemView>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = VideoItemView(parent.context)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        items.getOrNull(position)?.let { data ->
            (holder.itemView as? VideoItemView)?.let {
                it.bindData(data)
                views.add(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setData(data: List<VideoItemData>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun start(position: Int) {
        Log.d("onuszhao", "VideoAdapter  start  position=$position")
        views.getOrNull(position)?.let {
            it.start()
        }
    }

    fun stop(position: Int) {
        Log.d("onuszhao", "VideoAdapter  stop  position=$position")
        views.getOrNull(position)?.let {
            it.stop()
        }
    }

    fun destroy(){
        views.forEach {
            it.destroy()
        }
    }

    inner class VideoViewHolder(itemView: VideoItemView) : RecyclerView.ViewHolder(itemView)
}