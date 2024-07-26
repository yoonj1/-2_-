package com.example.guru2_cleanspirit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View

class CircularTimerView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var timeLeftInMillis: Long = 0
    private var timer: CountDownTimer? = null
    private val paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 15f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun startTimer(timeInMillis: Long) {
        timer?.cancel()
        timeLeftInMillis = timeInMillis
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                invalidate()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                invalidate()
            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel()
    }

    fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = 0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = width / 2f - paint.strokeWidth / 2
        val angle = 360 * (timeLeftInMillis / 1000f) / (60 * 60)
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        canvas.drawArc(
            paint.strokeWidth / 2, paint.strokeWidth / 2, width - paint.strokeWidth / 2,
            height - paint.strokeWidth / 2, -90f, angle, false, paint
        )
    }
}
