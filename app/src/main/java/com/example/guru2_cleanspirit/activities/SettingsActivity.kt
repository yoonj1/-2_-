package com.example.guru2_cleanspirit

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guru2_cleanspirit.src.DBHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var backButton: ImageButton
    private lateinit var switchService: Switch
    private lateinit var dbHelper: DBHelper

    // 현재 로그인한 사용자 이름 (예제에서는 하드코딩, 실제로는 로그인 세션 등에서 가져와야 함)
    private val currentUsername = "current_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        listView = findViewById(R.id.listView)
        backButton = findViewById(R.id.btnBack)
        switchService = findViewById(R.id.switchService)

        // DBHelper 인스턴스 생성
        dbHelper = DBHelper(this)

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

        // 차단된 앱 목록 가져오기
        showBlockedApps()
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

    // 차단된 앱 목록 가져오기
    private fun showBlockedApps() {
        val blockedApps = dbHelper.getBlockedApps(currentUsername)

        if (blockedApps.isEmpty()) {
            Toast.makeText(this, "차단된 앱이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val blockedAppInfos = blockedApps.map { packageName ->
            val appName = getAppName(packageName)
            val icon = getAppIcon(packageName)
            AppInfo(appName, packageName, icon)
        }

        // 차단된 앱 어댑터 초기화
        val blockedAdapter = AppListAdapter(this, blockedAppInfos)
        listView.adapter = blockedAdapter
    }

    // 앱 이름 가져오기
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    // 앱 아이콘 가져오기
    private fun getAppIcon(packageName: String): Drawable {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            getDrawable(R.mipmap.ic_launcher) ?: error("Default icon not found")
        }
    }
}
