package com.example.guru2_cleanspirit

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // UI 요소를 위한 변수 선언
    private lateinit var timerText: EditText
    private lateinit var btnPomodoro40: Button
    private lateinit var btnPomodoro50: Button
    private lateinit var btnMathExam: Button
    private lateinit var btnKoreanExam: Button
    private lateinit var btnDelete: ImageView
    private lateinit var btnPause: ImageView
    private lateinit var btnReload: ImageView
    private lateinit var settingsButton: ImageView

    // 타이머와 관련된 변수 선언
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var initialTimeInMillis: Long = 0
    private var isTimerRunning: Boolean = false

    // 원형 진행 표시기를 위한 변수 선언
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 요소 초기화
        timerText = findViewById(R.id.timerText)
        btnPomodoro40 = findViewById(R.id.btnPomodoro40)
        btnPomodoro50 = findViewById(R.id.btnPomodoro50)
        btnMathExam = findViewById(R.id.btnMathExam)
        btnKoreanExam = findViewById(R.id.btnKoreanExam)
        btnDelete = findViewById(R.id.btnDelete)
        btnPause = findViewById(R.id.btnPause)
        btnReload = findViewById(R.id.btnReload)
        settingsButton = findViewById(R.id.settingsButton)

        progressBar = findViewById(R.id.progressBar)

        // 설정 버튼 클릭 리스너 설정
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // 버튼 클릭 리스너 설정
        btnPomodoro40.setOnClickListener { startTimer(40 * 60 * 1000) }
        btnPomodoro50.setOnClickListener { startTimer(50 * 60 * 1000) }
        btnMathExam.setOnClickListener { startTimer(60 * 60 * 1000) }
        btnKoreanExam.setOnClickListener { startTimer(60 * 60 * 1000) }
        btnDelete.setOnClickListener { resetTimer() }
        btnPause.setOnClickListener { toggleTimer() }
        btnReload.setOnClickListener { reloadTimer() }
    }

    private fun startTimer(timeInMillis: Long) {
        countDownTimer?.cancel()
        initialTimeInMillis = timeInMillis
        timeLeftInMillis = timeInMillis
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                updateProgressBar()
            }

            override fun onFinish() {
                // 타이머가 종료되었을 때의 동작을 정의합니다.
            }
        }.start()
        isTimerRunning = true
    }

    private fun toggleTimer() {
        if (isTimerRunning) {
            pauseTimer()
        } else {
            startTimer(timeLeftInMillis)
        }
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timeLeftInMillis = 0
        updateTimerText()
        updateProgressBar()
    }

    private fun reloadTimer() {
        startTimer(timeLeftInMillis)
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerText.setText(timeFormatted)
    }

    private fun updateProgressBar() {
        val progress = (timeLeftInMillis.toFloat() / initialTimeInMillis.toFloat()) * 100
        progressBar.progress = progress.toInt()
    }
}
