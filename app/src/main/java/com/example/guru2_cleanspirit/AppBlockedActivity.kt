package com.example.guru2_cleanspirit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppBlockedActivity : AppCompatActivity() {

    private lateinit var timeLeftTextView: TextView
    private lateinit var circularTimerView: ProgressBar
    private var timerService: TimerService? = null

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val timeLeft = intent.getLongExtra("timeLeft", 0)
            updateTimeLeftTextView(timeLeft)
            updateCircularTimerView(timeLeft)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_app)

        val blockedMessageTextView: TextView = findViewById(R.id.blockedMessageTextView)
        blockedMessageTextView.text = "This app is blocked"

        timeLeftTextView = findViewById(R.id.timeLeftTextView)
        circularTimerView = findViewById(R.id.circularTimerView)

        val intent = Intent(this, TimerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName, service: android.os.IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            val timeLeft = timerService?.getTimeLeft() ?: 0
            updateTimeLeftTextView(timeLeft)
            updateCircularTimerView(timeLeft)
        }

        override fun onServiceDisconnected(name: android.content.ComponentName) {
            timerService = null
        }
    }

    private fun updateTimeLeftTextView(timeLeft: Long) {
        val minutes = (timeLeft / 1000) / 60
        val seconds = (timeLeft / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timeLeftTextView.text = timeFormatted
    }

    private fun updateCircularTimerView(timeLeft: Long) {
        val progress = if (timerService?.getInitialTimeInMillis() ?: 0 > 0) {
            (timeLeft.toFloat() / (timerService?.getInitialTimeInMillis() ?: 1).toFloat()) * 100
        } else {
            0f
        }
        circularTimerView.progress = progress.toInt()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(timerUpdateReceiver, IntentFilter("TIMER_UPDATED"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(timerUpdateReceiver)
        unbindService(serviceConnection)
    }
}
