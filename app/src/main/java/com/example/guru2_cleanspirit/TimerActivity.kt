package com.example.guru2_cleanspirit

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TimerActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var btnPomodoro40: Button
    private lateinit var btnPomodoro50: Button
    private lateinit var btnMathExam: Button
    private lateinit var btnKoreanExam: Button
    private lateinit var btnDelete: ImageView
    private lateinit var btnPause: ImageView
    private lateinit var btnReload: ImageView

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerText = findViewById(R.id.timerText)
        btnPomodoro40 = findViewById(R.id.btnPomodoro40)
        btnPomodoro50 = findViewById(R.id.btnPomodoro50)
        btnMathExam = findViewById(R.id.btnMathExam)
        btnKoreanExam = findViewById(R.id.btnKoreanExam)
        btnDelete = findViewById(R.id.btnDelete)
        btnPause = findViewById(R.id.btnPause)
        btnReload = findViewById(R.id.btnReload)

        btnPomodoro40.setOnClickListener { startTimer(40 * 60 * 1000) }
        btnPomodoro50.setOnClickListener { startTimer(50 * 60 * 1000) }
        btnMathExam.setOnClickListener { startTimer(60 * 60 * 1000) }
        btnKoreanExam.setOnClickListener { startTimer(60 * 60 * 1000) }
        btnDelete.setOnClickListener { resetTimer() }
        btnPause.setOnClickListener { pauseTimer() }
        btnReload.setOnClickListener { reloadTimer() }
    }

    private fun startTimer(timeInMillis: Long) {
        countDownTimer?.cancel()
        timeLeftInMillis = timeInMillis
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                // 타이머가 종료되었을 때의 동작을 정의합니다.
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timeLeftInMillis = 0
        updateTimerText()
    }

    private fun reloadTimer() {
        startTimer(timeLeftInMillis)
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerText.text = timeFormatted
    }
}
