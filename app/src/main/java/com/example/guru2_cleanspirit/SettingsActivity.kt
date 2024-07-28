package com.example.guru2_cleanspirit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchService: Switch
    private lateinit var recyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchService = findViewById(R.id.switchService)
        recyclerView = findViewById(R.id.recyclerView)

        // Home 버튼 처리
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하여 홈으로 돌아감
        }

        // 스위치 초기화
        val sharedPreferences = getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        switchService.isChecked = sharedPreferences.getBoolean("AppBlockerEnabled", false)
        switchService.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("AppBlockerEnabled", isChecked)
            editor.apply()

            // 서비스 시작 또는 중지
            if (isChecked) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            } else {
                stopService(Intent(this, AppBlockerService::class.java))
            }
        }

        // RecyclerView 초기화
        recyclerView.layoutManager = LinearLayoutManager(this)
        appListAdapter = AppListAdapter(this, getAppList(), isTimerRunning = true)
        recyclerView.adapter = appListAdapter
    }

    // 앱 목록 가져오기
    private fun getAppList(): List<AppInfo> {
        val packageManager = packageManager
        val apps = packageManager.getInstalledApplications(0)
        return apps.map { app ->
            AppInfo(
                app.loadLabel(packageManager).toString(),
                app.packageName,
                app.loadIcon(packageManager)
            )
        }.sortedBy { it.appName }
    }
}
