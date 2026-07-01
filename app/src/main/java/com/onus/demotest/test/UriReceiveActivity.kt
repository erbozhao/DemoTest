package com.onus.demotest.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class UriReceiveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.setPadding(32, 32, 32, 32)
        setContentView(textView)

        Log.e("onuszhao", "UriReceiveActivity  start")

        var uri: Uri? = null
        if (intent.action == Intent.ACTION_SEND) {
            // Android 13+ recommended approach:
            uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            uri = intent.data
        }

        val hasReadPermissionFlag = (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0

        if (uri != null) {
            val logBuilder = StringBuilder("Received URI: $uri\n")
            logBuilder.append("Has FLAG_GRANT_READ_URI_PERMISSION: $hasReadPermissionFlag\n\n")
            try {
                // Testing if reading the URI without explicit FLAG_GRANT_READ_URI_PERMISSION causes crash/exception
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val available = inputStream.available()
                    logBuilder.append("Success! Read $available bytes from URI.\n")
                    inputStream.close()
                } else {
                    logBuilder.append("Failed to open InputStream (returned null).\n")
                }
            } catch (e: SecurityException) {
                logBuilder.append("SecurityException caught!\n${e.message}\n")
                Log.e("onuszhao", "SecurityException reading URI", e)
            } catch (e: Exception) {
                logBuilder.append("Exception caught!\n${e.message}\n")
                Log.e("onuszhao", "Exception reading URI", e)
            }
            textView.text = logBuilder.toString()
            Log.e("onuszhao", "UriReceiveActivity  \n${logBuilder}")
        } else {
            textView.text = "No URI received (Action: ${intent.action})\nHas FLAG_GRANT_READ_URI_PERMISSION: $hasReadPermissionFlag"
            Log.e("onuszhao", "UriReceiveActivity  ${textView.text}")
        }
        Log.e("onuszhao", "UriReceiveActivity  end")
    }
}
