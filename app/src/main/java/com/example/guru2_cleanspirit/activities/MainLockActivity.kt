package com.example.guru2_cleanspirit

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class MainLockActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutMs: Long = 30000 // 기본값 30초
    private val lockRunnable = Runnable { lockDevice() }
    private val REQUEST_CODE_DEVICE_ADMIN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        val timeEditText: EditText = findViewById(R.id.timeEditText)
        val startButton: Button = findViewById(R.id.startButton)
        val backButton: ImageButton = findViewById(R.id.btnBack)

        startButton.setOnClickListener {
            // 시간 입력 확인 및 설정
            val timeString = timeEditText.text.toString()
            if (timeString.isNotEmpty()) {
                try {
                    val timeInSeconds = timeString.toLong()
                    timeoutMs = timeInSeconds * 1000 // 초를 밀리초로 변환
                    // 설정 완료 메시지 표시
                    Toast.makeText(this, "설정 완료! ${timeInSeconds}초 후에 잠금화면으로 전환됩니다.", Toast.LENGTH_SHORT).show()
                    // 디바이스 관리자 권한 요청
                    requestDeviceAdminPermission()
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
                lockDevice()
            } else {
                // 권한이 허용되지 않음
                // 사용자에게 권한이 필요하다는 메시지를 표시할 수 있습니다.
                Toast.makeText(this, "디바이스 관리자 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
