package com.example.guru2_cleanspirit

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var backButton: ImageButton
    private lateinit var switchService: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        listView = findViewById(R.id.listView)
        backButton = findViewById(R.id.btnBack)
        switchService = findViewById(R.id.switchService)

        // 뒤로 가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener {
            finish() // 현재 액티비티를 종료하여 이전 액티비티로 돌아갑니다.
        }

        // switchService 클릭 리스너 설정
        switchService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 권한이 없을 때 권한 설정 페이지로 이동
                if (!isAccessibilityServiceEnabled(this)) {
                    Toast.makeText(this, "Accessibility 권한을 설정해주세요.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                    switchService.isChecked = false // 권한을 받기 전까지는 체크 해제 상태로 유지
                }
            } else {
                // 권한 해제 로직 추가 (필요한 경우)
            }
        }

        // 실제 앱 목록 가져오기
        val appList = getAppList()

        // 어댑터 초기화
        val adapter = AppListAdapter(this, appList)
        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // 접근성 서비스 권한이 활성화되어 있는지 확인
        switchService.isChecked = isAccessibilityServiceEnabled(this)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (!enabledServices.isNullOrEmpty()) {
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals("${context.packageName}/com.example.guru2_cleanspirit.services.AppBlockerService", ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getAppList(): List<AppInfo> {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appList = mutableListOf<AppInfo>()

        for (app in apps) {
            val appName = app.loadLabel(pm).toString()
            val packageName = app.packageName
            val icon = app.loadIcon(pm)
            appList.add(AppInfo(appName, packageName, icon))
        }

        return appList
    }
}
