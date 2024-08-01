package com.example.guru2_cleanspirit

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.example.guru2_cleanspirit.src.DBHelper

class MainActivity : AppCompatActivity() {

    private lateinit var timeEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPomodoro40: Button
    private lateinit var btnPomodoro50: Button
    private lateinit var btnMathExam: Button
    private lateinit var btnKoreanExam: Button
    private lateinit var btnDelete: ImageView
    private lateinit var pauseButton: ImageView
    private lateinit var btnReload: ImageView
    private lateinit var settingsButton: ImageButton

    private var timerService: TimerService? = null
    private var isServiceBound = false
    private var initialTimeInMillis: Long = 0
    private var isTimerPaused = false

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutMs: Long = 30000 // 기본값 30초
    private val lockRunnable = Runnable { lockDevice() }
    private val REQUEST_CODE_DEVICE_ADMIN = 1

    private lateinit var dbHelper: DBHelper
    private val currentUsername = "current_user"

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

        timeEditText = findViewById(R.id.timerText)
        progressBar = findViewById(R.id.progressBar)
        btnPomodoro40 = findViewById(R.id.btnPomodoro40)
        btnPomodoro50 = findViewById(R.id.btnPomodoro50)
        btnMathExam = findViewById(R.id.btnMathExam)
        btnKoreanExam = findViewById(R.id.btnKoreanExam)
        btnDelete = findViewById(R.id.btnDelete)
        pauseButton = findViewById(R.id.btnPause)
        btnReload = findViewById(R.id.btnReload)
        settingsButton = findViewById(R.id.settingsButton)

        dbHelper = DBHelper(this)

        settingsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        btnPomodoro40.setOnClickListener {
            startTimer(40 * 60 * 1000L)
        }

        btnPomodoro50.setOnClickListener {
            startTimer(50 * 60 * 1000L)
        }

        btnMathExam.setOnClickListener {
            startTimer(60 * 60 * 1000L)
        }

        btnKoreanExam.setOnClickListener {
            startTimer(80 * 60 * 1000L)
        }

        btnDelete.setOnClickListener {
            resetTimer()
        }

        pauseButton.setOnClickListener {
            if (isTimerPaused) {
                resumeTimer()
            } else {
                pauseTimer()
            }
        }

        btnReload.setOnClickListener {
            resetTimer()
        }

        // Service binding
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // 차단된 앱 목록 표시
        showBlockedApps()
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_lock -> {
                    val intent = Intent(this, LockActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun startTimer(timeInMillis: Long) {
        initialTimeInMillis = timeInMillis
        updateNotification(timeInMillis)
        timerService?.startTimer(timeInMillis)
        updateTimerText(timeInMillis)
        updateProgressBar(timeInMillis)
    }

    private fun resetTimer() {
        initialTimeInMillis = 0
        updateTimerText(0)
        updateProgressBar(0)
        timerService?.resetTimer()
    }

    private fun pauseTimer() {
        timerService?.pauseTimer()
        isTimerPaused = true
        pauseButton.setImageResource(R.drawable.pause) // Ensure pause icon is in res/drawable
    }

    private fun resumeTimer() {
        timerService?.resumeTimer()
        isTimerPaused = false
        pauseButton.setImageResource(R.drawable.pause) // Ensure pause icon is in res/drawable
    }

    private fun updateTimerText(timeLeft: Long) {
        val minutes = (timeLeft / 1000) / 60
        val seconds = (timeLeft / 1000) % 60
        timeEditText.setText(String.format("%02d:%02d", minutes, seconds))
    }

    private fun updateProgressBar(timeLeft: Long) {
        progressBar.progress = ((timeLeft.toFloat() / initialTimeInMillis) * 100).toInt()
    }

    private fun lockDevice() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(componentName)) {
            dpm.lockNow()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DEVICE_ADMIN) {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(componentName)) {
                handler.postDelayed(lockRunnable, timeoutMs)
            } else {
                Toast.makeText(this, "디바이스 관리자 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBlockedApps() {
        val blockedApps = dbHelper.getBlockedApps(currentUsername)
        if (blockedApps.isEmpty()) {
            Toast.makeText(this, "차단된 앱이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val blockedAppInfos = blockedApps.map { packageName ->
            val appName = getAppName(packageName)
            val icon = getAppIcon(packageName)
            AppInfo(appName, packageName, icon)
        }

        val blockedAdapter = AppListAdapter(this, blockedAppInfos)
        // 필요한 곳에 blockedAdapter를 설정합니다.
        // 예를 들어, listView.adapter = blockedAdapter
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun getAppIcon(packageName: String): Drawable {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            ContextCompat.getDrawable(this, R.mipmap.ic_launcher) ?: error("Default icon not found")
        }
    }

    private fun updateNotification(timeLeft: Long) {
        val intent = Intent(this, TimerService::class.java)
        intent.putExtra("timeInMillis", timeLeft)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
