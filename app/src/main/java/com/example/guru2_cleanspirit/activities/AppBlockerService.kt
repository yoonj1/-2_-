package com.example.guru2_cleanspirit.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.guru2_cleanspirit.AppBlockedActivity
import com.example.guru2_cleanspirit.MainActivity
import com.example.guru2_cleanspirit.R
import com.example.guru2_cleanspirit.RestartServiceReceiver
import java.util.concurrent.Executors

class AppBlockerService : AccessibilityService() {

    private lateinit var sharedPreferences: SharedPreferences
    private val CHANNEL_ID = "AppBlockerServiceChannel"
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("AppBlockerPreferences", MODE_PRIVATE)
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification) // startForeground 호출
        Log.d("AppBlockerService", "Service created and started in foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification) // startForeground 호출
        Log.d("AppBlockerService", "startForeground called in onStartCommand")

        return START_STICKY
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = null
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.d("AppBlockerService", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            Log.d("AppBlockerService", "Window state changed: $packageName")
            if (packageName != null && isAppBlocked(packageName)) {
                Log.d("AppBlockerService", "Blocked app detected: $packageName")
                showBlockScreen(packageName)
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AppBlockerService", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AppBlockerService", "Service destroyed, restarting...")
        val broadcastIntent = Intent(this, RestartServiceReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    private fun isAppBlocked(packageName: String?): Boolean {
        return packageName != null && sharedPreferences.getBoolean(packageName, false)
    }

    private fun showBlockScreen(packageName: String) {
        Log.d("AppBlockerService", "Showing block screen")

        executor.execute {
            try {
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.killBackgroundProcesses(packageName)
                Log.d("AppBlockerService", "Killed background processes for $packageName")
            } catch (e: Exception) {
                Log.e("AppBlockerService", "Error killing background processes", e)
            }
        }

        Handler(Looper.getMainLooper()).post {
            try {
                val intent = Intent(this, AppBlockedActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                Log.d("AppBlockerService", "Started AppBlockedActivity")
            } catch (e: Exception) {
                Log.e("AppBlockerService", "Error starting AppBlockedActivity", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "App Blocker Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocker Service")
            .setContentText("Monitoring app usage")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        fun blockApp(context: Context, packageName: String) {
            val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean(packageName, true)
                apply()
            }
        }

        fun unblockApp(context: Context, packageName: String) {
            val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean(packageName, false)
                apply()
            }
        }
    }
}
