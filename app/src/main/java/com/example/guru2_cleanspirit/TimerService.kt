package com.example.guru2_cleanspirit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TimerService : Service() {

    private val binder = TimerBinder()
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var initialTimeInMillis: Long = 0

    companion object {
        const val CHANNEL_ID = "TimerServiceChannel"
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timer Service")
            .setContentText("Timer is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun startTimer(timeInMillis: Long) {
        initialTimeInMillis = timeInMillis
        timer?.cancel()
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val intent = Intent("TIMER_UPDATED")
                intent.putExtra("timeLeft", timeLeftInMillis)
                sendBroadcast(intent)
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                val intent = Intent("TIMER_UPDATED")
                intent.putExtra("timeLeft", timeLeftInMillis)
                sendBroadcast(intent)
            }
        }.start()
    }

    fun stopTimer() {
        timer?.cancel()
        timeLeftInMillis = 0
        val intent = Intent("TIMER_UPDATED")
        intent.putExtra("timeLeft", timeLeftInMillis)
        sendBroadcast(intent)
    }

    fun pauseTimer() {
        timer?.cancel()
    }

    fun resumeTimer() {
        startTimer(timeLeftInMillis)
    }

    fun resetTimer() {
        startTimer(initialTimeInMillis)
    }

    fun getTimeLeft(): Long {
        return timeLeftInMillis
    }

    fun getInitialTimeInMillis(): Long {
        return initialTimeInMillis
    }
}
