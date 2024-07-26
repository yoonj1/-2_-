package com.example.guru2_cleanspirit

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class BlockedActivity : AccessibilityService() {

    private val blockedApps = listOf("com.google.android.youtube", "blocked.app.package")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (blockedApps.contains(packageName)) {
                // 금지된 앱이 실행된 경우 TimerActivity를 시작
                val intent = Intent(this, TimerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // 인터럽트 처리
    }
}
