package com.example.guru2_cleanspirit.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.ActivityManager
import com.example.guru2_cleanspirit.ui.PopupActivity

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == "android.intent.action.MY_PACKAGE_REPLACED") {
            if (isAppRunning(context, "com.example.targetapp")) {
                showPopup(context)
            }
        }
    }

    private fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activityManager.runningAppProcesses.any { it.processName == packageName }
        } else {
            false
        }
    }

    private fun showPopup(context: Context) {
        val intent = Intent(context, PopupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
