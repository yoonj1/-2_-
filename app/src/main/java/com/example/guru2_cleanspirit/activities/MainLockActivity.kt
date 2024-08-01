package com.example.guru2_cleanspirit

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainLockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutMs: Long = 30000 // 기본값 30초
    private val lockRunnable = Runnable { lockDevice() }
    private val REQUEST_CODE_DEVICE_ADMIN = 1

    private lateinit var timeEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var lockToggle: Switch
    private var initialTimeInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        timeEditText = findViewById(R.id.timerText)
        progressBar = findViewById(R.id.progressBar)
        lockToggle = findViewById(R.id.lockToggle)
        val startButton: Button = findViewById(R.id.startButton)
        val backButton: ImageButton = findViewById(R.id.btnBack)

        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        startButton.setOnClickListener {
            // 시간 입력 확인 및 설정
            val timeString = timeEditText.text.toString()
            if (timeString.isNotEmpty()) {
                try {
                    val timeParts = timeString.split(":").map { it.toInt() }
                    val minutes = timeParts[0]
                    val seconds = timeParts[1]
                    val timeInSeconds = minutes * 60 + seconds
                    timeoutMs = timeInSeconds * 1000L // 초를 밀리초로 변환
                    // 설정 완료 메시지 표시
                    Toast.makeText(this, "설정 완료! ${timeInSeconds}초 후에 잠금화면으로 전환됩니다.", Toast.LENGTH_SHORT).show()
                    // 디바이스 관리자 권한 요청
                    if (lockToggle.isChecked) {
                        requestDeviceAdminPermission()
                    } else {
                        startTimer(timeoutMs)
                    }
                } catch (e: NumberFormatException) {
                    // 유효하지 않은 입력
                    Toast.makeText(this, "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "시간을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 화면으로 돌아갑니다.
        }
    }

    private fun showTimePickerDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_time_input, null)
        val minutesInput = view.findViewById<EditText>(R.id.minutesInput)
        val secondsInput = view.findViewById<EditText>(R.id.secondsInput)

        AlertDialog.Builder(this)
            .setTitle("시간 설정")
            .setView(view)
            .setPositiveButton("확인") { _, _ ->
                val minutes = minutesInput.text.toString().toIntOrNull() ?: 0
                val seconds = secondsInput.text.toString().toIntOrNull() ?: 0
                val timeInMillis = (minutes * 60 + seconds) * 1000L
                timeEditText.setText(String.format("%02d:%02d", minutes, seconds))
                startTimer(timeInMillis)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun startTimer(timeInMillis: Long) {
        initialTimeInMillis = timeInMillis
        updateTimerText(initialTimeInMillis)
        updateProgressBar(initialTimeInMillis)

        handler.postDelayed(lockRunnable, timeInMillis)

        val progressBarUpdater = object : Runnable {
            override fun run() {
                val elapsed = initialTimeInMillis - timeoutMs
                updateProgressBar(elapsed)
                if (timeoutMs > 0) {
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(progressBarUpdater)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DEVICE_ADMIN) {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(componentName)) {
                // 권한이 허용됨
                handler.postDelayed(lockRunnable, timeoutMs)
            } else {
                // 권한이 허용되지 않음
                // 사용자에게 권한이 필요하다는 메시지를 표시할 수 있습니다.
                Toast.makeText(this, "디바이스 관리자 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
