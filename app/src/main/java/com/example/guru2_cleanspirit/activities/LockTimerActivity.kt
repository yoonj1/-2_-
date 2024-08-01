package com.example.guru2_cleanspirit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Switch
import android.widget.TextView

class LockTimerActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private lateinit var lockToggle: Switch
    private var isTimerRunning = false
    private var startTime = 0L
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - startTime
            val seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            val hours = minutes / 60
            timerText.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        timerText = findViewById(R.id.timerText)
        startButton = findViewById(R.id.startButton)
        lockToggle = findViewById(R.id.lockToggle)

        startButton.setOnClickListener {
            if (isTimerRunning) {
                timerHandler.removeCallbacks(timerRunnable)
                startButton.text = "Start Timer"
            } else {
                startTime = System.currentTimeMillis()
                timerHandler.postDelayed(timerRunnable, 0)
                startButton.text = "Stop Timer"
            }
            isTimerRunning = !isTimerRunning
        }

        lockToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 스크린 잠금 기능 구현
                // 예를 들어, 화면을 잠그는 액션을 트리거
            } else {
                // 스크린 잠금 기능 비활성화
                // 잠금을 해제하는 액션을 트리거
            }
        }
    }
}
