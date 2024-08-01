package com.example.guru2_cleanspirit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppBlockedActivity : AppCompatActivity() {

    private lateinit var timeLeftTextView: TextView
    private lateinit var circularTimerView: ProgressBar
    private var timerService: TimerService? = null

    private lateinit var listView: ListView

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

        timeLeftTextView = findViewById(R.id.timeLeftTextView)
        circularTimerView = findViewById(R.id.circularTimerView)

        listView = findViewById(R.id.listView)

        val blockedMessageTextView: TextView = findViewById(R.id.blockedMessageTextView)
        blockedMessageTextView.text = "This app is blocked"

        val appList = getAppList()

        val adapter = AppListAdapter(this, appList)
        listView.adapter = adapter

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
        // progress bar 업데이트 로직을 수정합니다.
        val progress = if (timeLeft > 0) {
            (timeLeft.toFloat() / timeLeft.toFloat()) * 100
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

    private fun getAppList(): List<AppInfo> {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appList = mutableListOf<AppInfo>()

        for (app in apps) {
            val appName = app.loadLabel(pm).toString()
            val packageName = app.packageName
            val icon = app.loadIcon(pm)
            appList.add(AppInfo(appName, packageName, icon))
        }

        return appList
    }
}
