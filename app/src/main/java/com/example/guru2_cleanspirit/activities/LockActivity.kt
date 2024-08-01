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

class LockActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private val timeoutMs: Long = 30000 // 30초
    private val lockRunnable = Runnable { lockDevice() }
    private val REQUEST_CODE_DEVICE_ADMIN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            // 디바이스 관리자 권한 요청
            requestDeviceAdminPermission()
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
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "이 앱은 30초 후에 자동으로 잠금화면으로 전환됩니다.")
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
                lockDevice()
            } else {
                // 권한이 허용되지 않음
                // 사용자에게 권한이 필요하다는 메시지를 표시할 수 있습니다.
            }
        }
    }
}
