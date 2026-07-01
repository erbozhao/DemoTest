package com.onus.demotest.feature.notification

/**
 * @Author: onuszhao
 * @Date: 2025-01-06 21:34
 * @Description:
 */
import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.common.DeviceUtils

/**
 * @Author: onuszhao
 * @Date: 2025-01-03 14:46
 * @Description:
 */
class PostNotificationActivity: AppCompatActivity(), View.OnClickListener{

    private lateinit var container: ScrollView
    private lateinit var requestBtn: Button

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEdit: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        container = ScrollView(this).apply {
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@PostNotificationActivity), 0, 0)
        }
        setContentView(container)
        ActivityStateManager.setCurActivity(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTheme(R.style.ActivityMain_Light)
        } else {
            setTheme(R.style.ActivityMain)
        }

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.color.white)
            container.addView(this, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ))
        }


        requestBtn = Button(this).apply {
            isAllCaps = false
            text = "Request Post Notify"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@PostNotificationActivity)
        }
        wrapper.addView(requestBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))


        sharedPreferences = getSharedPreferences("post_notify_preferences", MODE_PRIVATE)
        sharedPreferencesEdit = sharedPreferences.edit()
    }

    override fun onClick(v: View?) {
        v?:return
        when(v) {
            requestBtn -> {
                requestPermission()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("onuszhao", "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("onuszhao", "onStop")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d("onuszhao", "onWindowFocusChanged  hasFocus=$hasFocus")
    }

    private var backPressed = false
    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("onuszhao", "onBackPressed  backPressed=$backPressed")
        backPressed = true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        KeyEvent.ACTION_DOWN
        KeyEvent.KEYCODE_BACK
        Log.d("onuszhao", "dispatchKeyEvent  action=${event?.action}  ${event?.keyCode}")
        return super.dispatchKeyEvent(event)
    }


    /** SDK33时，请求运行时通知权限 */
    private fun requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val hasPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
            val requestNum = sharedPreferences.getLong("request_post_notify_num", 0)
            val hasDeniedPermission = sharedPreferences.getBoolean("request_post_notify_denied", false)
            if (!hasPermission){
                if (shouldShow){
                    // 拒绝过一次，再次请求
                    Log.d("onuszhao", "requestPermission  仅拒绝过一次  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                    sharedPreferencesEdit.putBoolean("request_post_notify_denied", true)
                    sharedPreferencesEdit.apply()
                } else {
                    if (requestNum > 0) {
                        if (hasDeniedPermission){
                            // 拒绝过多次次，请求也调不起来
                            Log.d("onuszhao", "requestPermission  拒绝过多次  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission")
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                        } else {
                            // 未拒绝过，可以调起来
                            Log.d("onuszhao", "requestPermission  未拒绝过  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission")
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                        }
                    } else {
                        // 首发请求
                        Log.d("onuszhao", "requestPermission  首次  requestNum=$requestNum  hasPermission=$hasPermission shouldShow=$shouldShow  hasDeniedPermission=$hasDeniedPermission")
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                    }
                }
            } else {
                Log.d("onuszhao", "requestPermission 已授权")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("onuszhao", "onRequestPermissionsResult  requestCode=$requestCode  permissions=${permissions.joinToString(",")}  grantResults=${grantResults.joinToString(",")}")
        if (requestCode == REQUEST_CODE){
            val hasPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
            val oldNum = sharedPreferences.getLong("request_post_notify_num", 0)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("onuszhao", "grant  success  oldNum=$oldNum  shouldShow=$shouldShow  hasPermission=$hasPermission")
            } else {
                Log.d("onuszhao", "grant  failed  oldNum=$oldNum   shouldShow=$shouldShow  hasPermission=$hasPermission")
            }

            // 记录弹出次数
            sharedPreferencesEdit.putLong("request_post_notify_num", oldNum + 1)
            sharedPreferencesEdit.apply()
        }
    }

    companion object{
        private const val REQUEST_CODE = 1000
    }

}