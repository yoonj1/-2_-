package com.example.guru2_cleanspirit

import android.Manifest
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat

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
    private lateinit var settingsButton: ImageView

    private var timerService: TimerService? = null
    private var isServiceBound = false
    private var initialTimeInMillis: Long = 0
    private var isTimerPaused = false

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutMs: Long = 30000 // 기본값 30초
    private val lockRunnable = Runnable { lockDevice() }
    private val REQUEST_CODE_DEVICE_ADMIN = 1

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
            updateNotification(timeLeft)
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

        timeEditText.setOnClickListener {
            showTimeInputDialog()
        }

        settingsButton.setOnClickListener {
            showPopupMenu(it)
        }

        updateTimerText(0)
        updateProgressBar(0)
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
        val intent = Intent(this, TimerService::class.java).apply {
            putExtra("timeInMillis", timeInMillis)
        }
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
        timeEditText.setText(timeFormatted)
    }

    private fun updateProgressBar(timeLeftInMillis: Long) {
        val progress = if (initialTimeInMillis > 0) {
            (timeLeftInMillis.toFloat() / initialTimeInMillis.toFloat()) * 100
        } else {
            0f
        }
        progressBar.progress = progress.toInt()
    }

    private fun lockDevice() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (dpm.isAdminActive(componentName)) {
            // 디바이스 관리자로 활성화됨
            try {
                dpm.lockNow()
            } catch (e: SecurityException) {
                e.printStackTrace()
                // 예외 처리: 사용자가 디바이스 관리자 권한을 활성화하지 않았을 경우
                // 권한 요청 화면으로 이동
                requestDeviceAdminPermission()
            }
        } else {
            // 권한 요청 화면으로 이동
            requestDeviceAdminPermission()
        }
    }

    private fun requestDeviceAdminPermission() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (dpm.isAdminActive(componentName)) {
            // 디바이스 관리자로 활성화됨
            handler.postDelayed(lockRunnable, timeoutMs)
        } else {
            // 권한 요청 화면으로 이동
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "이 앱은 설정한 시간 후에 자동으로 잠금화면으로 전환됩니다.")
            }
            startActivityForResult(intent, REQUEST_CODE_DEVICE_ADMIN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DEVICE_ADMIN) {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(componentName)) {
                // 권한이 허용됨
                lockDevice()
            } else {
                // 권한이 허용되지 않음
                // 사용자에게 권한이 필요하다는 메시지를 표시할 수 있습니다.
                Toast.makeText(this, "디바이스 관리자 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
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
