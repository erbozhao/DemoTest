package com.onus.demotest.pages.video

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.onus.demotest.R
import com.onus.demotest.core.KernelEngine
import com.onus.demotest.utils.DeviceUtils

/**
 * @Author: onuszhao
 * @Date: 2023-11-08 16:33
 * @Description:
 */
class VideoActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var container: LinearLayout
    private var refresh: TextView? = null

    private lateinit var viewPager2: ViewPager2
    private lateinit var videoAdapter: VideoAdapter

    /** 当前滑动方向  */
    private var scrollDirection = SCROLL_DOWN

    /** 滑动偏移量百分比，下滑为0->1，上滑1->0  */
    private var lastPositionOffset = 0f

    /** 当前手势滑动方向  */
    private var fingerScrollDirection = SCROLL_DOWN

    /** 当前按下的位置  */
    private var lastPosY = 0f

    /** 记录上一次位置，防止onPageSelected重复调 */
    private var lastPosition = -1

    /** 滑动过程position */
    private var scrollPosition = -1

    /** 记录滑动状态 */
    private var oldStage = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // 设置自定义标题栏布局
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_titlebar)

        container = LinearLayout(this).apply {
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@VideoActivity), 0, 0)
        }
        setContentView(container)

        supportActionBar?.customView?.findViewById<TextView>(R.id.title)?.also {
            it.text = if (KernelEngine.channelType.isTranssionBuildin()) {
                "transsionBuildin"
            } else if (KernelEngine.channelType.isTranssionPreinstall()) {
                "transsionPreinstall"
            } else if (KernelEngine.channelType.isVivoPreinstall()) {
                "vivoPreinstall"
            } else if (KernelEngine.channelType.isOppoPreinstall()) {
                "oppoPreinstall"
            } else if (KernelEngine.channelType.isSamsungPreinstall()) {
                "samsungPreinstall"
            } else {
                "normal"
            }
        }
        refresh = supportActionBar?.customView?.findViewById<TextView>(R.id.refresh)
        refresh?.setOnClickListener(this)

        viewPager2 = ViewPager2(this).apply {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
            container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        }

        videoAdapter = VideoAdapter().apply {
            viewPager2.adapter = this
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.d("onuszhao", "onPageScrolled  position=$position  curPosition=${getCurrentPosition()}")
                if (positionOffset == 0f) {
                    return
                }
                // 将滑动事件传递进去
                if (position == lastPosition) {
                    // videoAdapter.start(position)
                    Log.d("onuszhao", "onPageScrolled  start  curPosition=${getCurrentPosition()}")
                }
                if (positionOffset > lastPositionOffset) {
                    scrollDirection = SCROLL_DOWN
                } else {
                    scrollDirection = SCROLL_UP
                }
                Log.d(
                    "onuszhao",
                    "onPageScrolled  positionOffset=$positionOffset   scrollDirection=$scrollDirection  curPosition=${getCurrentPosition()}"
                )
                lastPositionOffset = positionOffset
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("onuszhao", "onPageSelected  position=$position    curPosition=${getCurrentPosition()}")
                //过滤重复回调
                if (lastPosition == position) {
                    return
                }

                //第一次进来的不属于页面切换
                if (lastPosition != -1) {
                    // 当页面切换时，需要停止上一个页面
                    Log.d("onuszhao", "onPageSelected  stop  lastPosition=$lastPosition    curPosition=${getCurrentPosition()}")
                    videoAdapter.stop(lastPosition)
                }
                lastPosition = position

                // 首次进入小视频NativePage时，激活当前页面
                videoAdapter.start(position)
                Log.d("onuszhao", "onPageSelected  start  position=$position    curPosition=${getCurrentPosition()}")
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.d("onuszhao", "onPageScrollStateChanged  state=$state    curPosition=${getCurrentPosition()}")
                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                    }

                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        scrollPosition = lastPosition
                    }
                }
            }
        })

        // viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        //     override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        //         super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        //         Log.d("onuszhao", "onPageScrolled  position=$position  curPosition=${getCurrentPosition()}")
        //         if (scrollPosition == getCurrentPosition()) {
        //             // 滑动过程更改背景等
        //         }
        //         lastPositionOffset = positionOffset
        //     }
        //
        //     override fun onPageSelected(position: Int) {
        //         super.onPageSelected(position)
        //         Log.d("onuszhao", "onPageSelected  position=$position    curPosition=${getCurrentPosition()}")
        //     }
        //
        //     override fun onPageScrollStateChanged(state: Int) {
        //         super.onPageScrollStateChanged(state)
        //         Log.d("onuszhao", "onPageScrollStateChanged  state=$state    curPosition=${getCurrentPosition()}")
        //         when (state) {
        //             ViewPager2.SCROLL_STATE_IDLE -> {
        //                 val position= getCurrentPosition()
        //                 //过滤重复回调
        //                 if (lastPosition == position) {
        //                     return
        //                 }
        //
        //                 //第一次进来的不属于页面切换
        //                 if (lastPosition != -1) {
        //                     // 当页面切换时，需要停止上一个页面
        //                     Log.d("onuszhao", "onPageScrollStateChanged  stop  lastPosition=$lastPosition    curPosition=${getCurrentPosition()}")
        //                     videoAdapter.stop(lastPosition)
        //                 }
        //                 lastPosition = position
        //
        //                 // 首次进入小视频NativePage时，激活当前页面
        //                 videoAdapter.start(position)
        //                 Log.d("onuszhao", "onPageScrollStateChanged  start  position=$position    curPosition=${getCurrentPosition()}")
        //                 lastPositionOffset = 0f
        //             }
        //             ViewPager2.SCROLL_STATE_DRAGGING -> {
        //                 scrollPosition = if (lastPosition < 0) 0 else lastPosition
        //             }
        //         }
        //         oldStage = state
        //     }
        // })

        loadData()
    }

    private fun loadData() {
        val demoVideos = listOf(
            VideoItemData("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            VideoItemData("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            VideoItemData("https://www.w3schools.com/html/mov_bbb.mp4"),
            VideoItemData("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"),
            VideoItemData("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_30MB.mp4")
        )
        videoAdapter.setData(demoVideos)
    }

    private fun getCurrentPosition(): Int {
        return viewPager2.currentItem
    }

    override fun onStart() {
        super.onStart()
        videoAdapter.start(getCurrentPosition())
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        videoAdapter.stop(getCurrentPosition())
    }

    override fun onDestroy() {
        super.onDestroy()
        videoAdapter.destroy()
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v) {
            refresh -> {
                loadData()  //数量一致，不会触发layout，不会回调onScroll
            }
        }
    }

    companion object {
        private const val SCROLL_DOWN = 1
        private const val SCROLL_UP = 0
    }
}
