package com.example.guru2_cleanspirit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TimerActivity : AppCompatActivity() {

    private lateinit var timerText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var timeLeftTextView: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button
    private var timeLeftInMillis: Long = 0
    private var initialTimeInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerText = findViewById(R.id.timerText)
        progressBar = findViewById(R.id.progressBar)
        timeLeftTextView = findViewById(R.id.timeLeftTextView)
        startButton = findViewById(R.id.startButton)
        pauseButton = findViewById(R.id.pauseButton)
        resetButton = findViewById(R.id.resetButton)

        // 전달받은 타이머 시간 설정
        timeLeftInMillis = intent.getLongExtra("TIME_LEFT", 0)
        initialTimeInMillis = intent.getLongExtra("INITIAL_TIME", 0)
        updateTimerText()
        updateProgressBar()

        // 버튼 클릭 리스너 설정
        startButton.setOnClickListener { startTimer() }
        pauseButton.setOnClickListener { pauseTimer() }
        resetButton.setOnClickListener { resetTimer() }

        // 타이머 완료 시 아래 코드 추가
        saveCurrentDate()
    }
    private fun saveCurrentDate() {
        val sharedPreferences = getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        val currentDate = Calendar.getInstance()
        val dateString = "${currentDate.get(Calendar.YEAR)}-${currentDate.get(Calendar.MONTH) + 1}-${currentDate.get(Calendar.DAY_OF_MONTH)}"

        val editor = sharedPreferences.edit()
        val savedDates = sharedPreferences.getStringSet("dates", mutableSetOf()) ?: mutableSetOf()
        savedDates.add(dateString)
        editor.putStringSet("dates", savedDates)
        editor.apply()
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

    private fun startTimer() {
        // 타이머 시작 로직 추가
    }

    private fun pauseTimer() {
        // 타이머 일시정지 로직 추가
    }

    private fun resetTimer() {
        // 타이머 초기화 로직 추가
    }
}
