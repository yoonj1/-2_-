package com.example.guru2_cleanspirit

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class LockActivity : AppCompatActivity() {

    private lateinit var timerText: EditText
    private lateinit var startButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutMs: Long = 30000 // 기본값 30초
    private val lockRunnable = Runnable { lockDevice() }
    private val REQUEST_CODE_DEVICE_ADMIN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        timerText = findViewById(R.id.timerText)
        startButton = findViewById(R.id.startButton)
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        timerText.setOnClickListener {
            showTimePickerDialog(it)
        }

        startButton.setOnClickListener {
            // 디바이스 관리자 권한 요청
            requestDeviceAdminPermission()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun requestDeviceAdminPermission() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, AdminReceiver::class.java)

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
        val componentName = ComponentName(this, AdminReceiver::class.java)

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
            val componentName = ComponentName(this, AdminReceiver::class.java)
            if (dpm.isAdminActive(componentName)) {
                // 권한이 허용됨
                handler.postDelayed(lockRunnable, timeoutMs)
            } else {
                // 권한이 허용되지 않음
                // 사용자에게 권한이 필요하다는 메시지를 표시할 수 있습니다.
            }
        }
    }

    fun showTimePickerDialog(view: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_input, null)

        val minutesInput = dialogView.findViewById<EditText>(R.id.minutesInput)
        val secondsInput = dialogView.findViewById<EditText>(R.id.secondsInput)

        AlertDialog.Builder(this)
            .setTitle("시간 설정")
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                val minutes = minutesInput.text.toString().toIntOrNull() ?: 0
                val seconds = secondsInput.text.toString().toIntOrNull() ?: 0
                timeoutMs = (minutes * 60 + seconds) * 1000L // 밀리초로 변환
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                timerText.setText(timeFormatted)
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
