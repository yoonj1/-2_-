package com.example.guru2_cleanspirit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TimerService : Service() {

    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    var initialTimeInMillis: Long = 0
        private set

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val timeInMillis = intent.getLongExtra("timeInMillis", 0)
        startForegroundService(timeInMillis)
        return START_NOT_STICKY
    }

    private fun startForegroundService(timeInMillis: Long) {
        initialTimeInMillis = timeInMillis
        timeLeftInMillis = timeInMillis

        createNotificationChannel()

        val notification = createNotification(timeInMillis)
        startForeground(1, notification)

        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateNotification(millisUntilFinished)
                val intent = Intent("TIMER_UPDATED").apply {
                    putExtra("timeLeft", millisUntilFinished)
                }
                sendBroadcast(intent)
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateNotification(timeLeftInMillis)
                val intent = Intent("TIMER_UPDATED").apply {
                    putExtra("timeLeft", timeLeftInMillis)
                }
                sendBroadcast(intent)
                stopSelf()
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "TIMER_CHANNEL",
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(timeInMillis: Long): Notification {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        return NotificationCompat.Builder(this, "TIMER_CHANNEL")
            .setContentTitle("Timer Service")
            .setContentText("Time left: $timeFormatted")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    private fun updateNotification(timeLeftInMillis: Long) {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        val notification = createNotification(timeLeftInMillis)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    fun startTimer(timeInMillis: Long) {
        startForegroundService(timeInMillis)
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        stopForeground(true)
        stopSelf()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
    }

    fun resumeTimer() {
        startForegroundService(timeLeftInMillis)
    }

    fun resetTimer() {
        stopTimer()
        timeLeftInMillis = 0
    }

    fun getTimeLeft(): Long {
        return timeLeftInMillis
    }
}
