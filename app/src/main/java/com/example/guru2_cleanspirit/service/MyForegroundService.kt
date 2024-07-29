package com.example.guru2_cleanspirit.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.guru2_cleanspirit.MainActivity
import com.example.guru2_cleanspirit.ui.PopupActivity

class MyForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foreground_service_channel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("Foreground Service")
            .setContentText("Service is running in the background")
            .setSmallIcon(R.drawable.ic_notification) // 이 부분이 올바르게 설정되었는지 확인하세요
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkAppRunning()
        return START_STICKY
    }

    private fun checkAppRunning() {
        val handler = android.os.Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isAppRunning(this@MyForegroundService, "com.example.targetapp")) {
                    showPopup()
                }
                handler.postDelayed(this, 5000) // 5초마다 체크
            }
        }, 5000)
    }

    private fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun showPopup() {
        val intent = Intent(this, PopupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
