package com.example.guru2_cleanspirit

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPomodoro40: Button
    private lateinit var btnPomodoro50: Button
    private lateinit var btnMathExam: Button
    private lateinit var btnKoreanExam: Button
    private lateinit var btnDelete: ImageView
    private lateinit var btnPause: ImageView
    private lateinit var btnReload: ImageView
    private lateinit var settingsButton: ImageView

    private var timerService: TimerService? = null
    private var isServiceBound = false
    private var initialTimeInMillis: Long = 0
    private var isTimerPaused = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isServiceBound = true
            updateTimerText(timerService?.getTimeLeft() ?: 0)
            updateProgressBar(timerService?.getTimeLeft() ?: 0)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            timerService = null
            isServiceBound = false
        }
    }

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val timeLeft = intent.getLongExtra("timeLeft", 0)
            updateTimerText(timeLeft)
            updateProgressBar(timeLeft)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        timerText = findViewById(R.id.timerText)
        progressBar = findViewById(R.id.progressBar)
        btnPomodoro40 = findViewById(R.id.btnPomodoro40)
        btnPomodoro50 = findViewById(R.id.btnPomodoro50)
        btnMathExam = findViewById(R.id.btnMathExam)
        btnKoreanExam = findViewById(R.id.btnKoreanExam)
        btnDelete = findViewById(R.id.btnDelete)
        btnPause = findViewById(R.id.btnPause)
        btnReload = findViewById(R.id.btnReload)
        settingsButton = findViewById(R.id.settingsButton)

        val intent = Intent(this, TimerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        btnPomodoro40.setOnClickListener {
            startTimer(40 * 60 * 1000)
        }

        btnPomodoro50.setOnClickListener {
            startTimer(50 * 60 * 1000)
        }

        btnMathExam.setOnClickListener {
            startTimer(100 * 60 * 1000)
        }

        btnKoreanExam.setOnClickListener {
            startTimer(80 * 60 * 1000)
        }

        btnDelete.setOnClickListener {
            stopAndResetTimer()
        }

        btnPause.setOnClickListener {
            if (isTimerPaused) {
                resumeTimer()
            } else {
                pauseTimer()
            }
        }

        btnReload.setOnClickListener {
            resetTimer()
        }

        timerText.setOnClickListener {
            showTimeInputDialog()
        }

        settingsButton.setOnClickListener {
            showPopupMenu(it)
        }

        updateTimerText(0)
        updateProgressBar(0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_calendar -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.main_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_calendar -> {
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showTimeInputDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_time_input, null)
        val minutesInput = view.findViewById<EditText>(R.id.minutesInput)
        val secondsInput = view.findViewById<EditText>(R.id.secondsInput)

        AlertDialog.Builder(this)
            .setTitle("시간 설정")
            .setView(view)
            .setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
                val minutes = minutesInput.text.toString().toIntOrNull() ?: 0
                val seconds = secondsInput.text.toString().toIntOrNull() ?: 0
                val timeInMillis = (minutes * 60 + seconds) * 1000L
                startTimer(timeInMillis)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun startTimer(timeInMillis: Long) {
        initialTimeInMillis = timeInMillis
        val intent = Intent(this, TimerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        timerService?.startTimer(timeInMillis)
        isTimerPaused = false
    }

    private fun stopAndResetTimer() {
        timerService?.stopTimer()
        updateTimerText(0)
        updateProgressBar(0)
        isTimerPaused = false
    }

    private fun pauseTimer() {
        timerService?.pauseTimer()
        isTimerPaused = true
    }

    private fun resumeTimer() {
        timerService?.resumeTimer()
        isTimerPaused = false
    }

    private fun resetTimer() {
        timerService?.resetTimer()
        updateTimerText(initialTimeInMillis)
        updateProgressBar(initialTimeInMillis)
        isTimerPaused = false
        startTimer(initialTimeInMillis)
    }

    private fun updateTimerText(timeLeftInMillis: Long) {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerText.setText(timeFormatted)
    }

    private fun updateProgressBar(timeLeftInMillis: Long) {
        val progress = if (initialTimeInMillis > 0) {
            (timeLeftInMillis.toFloat() / initialTimeInMillis.toFloat()) * 100
        } else {
            0f
        }
        progressBar.progress = progress.toInt()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(timerUpdateReceiver, IntentFilter("TIMER_UPDATED"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(timerUpdateReceiver)
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}
