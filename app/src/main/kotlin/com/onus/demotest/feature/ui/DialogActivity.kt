package com.onus.demotest.feature.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.onus.demotest.R
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.common.DeviceUtils

/**
 * @Author: onuszhao
 * @Date: 2025-01-03 14:46
 * @Description:
 */
class DialogActivity: AppCompatActivity(), View.OnClickListener{

    private lateinit var container: ScrollView
    private lateinit var dialogBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        Log.d("onuszhao", "DialogActivity onCreate")
        container = ScrollView(this).apply {
            setBackgroundResource(R.color.white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPaddingRelative(0, DeviceUtils.getStatusBarHeight(this@DialogActivity), 0, 0)
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


        dialogBtn = Button(this).apply {
            isAllCaps = false
            text = "Show Dialog"
            gravity = Gravity.CENTER
            textSize = resources.getDimension(R.dimen.dp_2)
            setOnClickListener(this@DialogActivity)
        }
        wrapper.addView(dialogBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("onuszhao", "DialogActivity onRestart")
    }

    override fun onStart() {
        super.onStart()
        Log.d("onuszhao", "DialogActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("onuszhao", "DialogActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("onuszhao", "DialogActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("onuszhao", "DialogActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onuszhao", "DialogActivity onDestroy")
    }

    override fun onClick(v: View?) {
        v?:return
        when(v) {
            dialogBtn -> {
                showDialog()
                // showDialogFragment()
            }
        }
    }

    private fun showDialog() {
        val dialog = TestDialog(this)
        val textView = TextView(this)
        textView.text = "这是一个Dialog弹窗"
        dialog.addContentView(textView, ViewGroup.LayoutParams(200, 200))
        dialog.show()
    }

    private fun showDialogFragment() {
        TestDialogFragment().show(supportFragmentManager, TestDialogFragment.TAG)
    }
}

class TestDialog(context: Context, them: Int = R.style.DialogActivity): Dialog(context, them){

    override fun dismiss() {
        kotlin.runCatching {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            intent.setClassName(context.packageName, DialogActivity::class.java.name)
            context.startActivity(intent)
        }
        super.dismiss()
    }
}

class TestDialogFragment : DialogFragment() {
    //利用AlertDialog或者Dialog创建出Dialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setMessage("这是一个DialogFragment弹窗")
            .setPositiveButton("OK") { _, _ ->

            }
            .create()
    }

    //使用定义的xml布局文件展示Dialog
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        const val TAG = "TestDialogFragment"
    }
}