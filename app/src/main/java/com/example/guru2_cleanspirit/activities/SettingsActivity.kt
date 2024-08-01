package com.example.guru2_cleanspirit

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.guru2_cleanspirit.src.DBHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private val currentUsername = "current_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        dbHelper = DBHelper(this)
        val listView: ListView = findViewById(R.id.listView)
        val backButton: ImageButton = findViewById(R.id.btnBack)
        val switchService: Switch = findViewById(R.id.switchService)

        backButton.setOnClickListener {
            finish()
        }

        switchService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 권한이 없을 때 권한 설정 페이지로 이동
                if (!isAccessibilityServiceEnabled(this)) {
                    Toast.makeText(this, "Accessibility 권한을 설정해주세요.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                    switchService.isChecked = false // 권한을 받기 전까지는 체크 해제 상태로 유지
                }
            }
        }

        showBlockedApps()
    }

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

        val blockedAdapter = AppListAdapter(this, blockedAppInfos)
        val listView: ListView = findViewById(R.id.listView)
        listView.adapter = blockedAdapter
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun getAppIcon(packageName: String): Drawable {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            ContextCompat.getDrawable(this, R.mipmap.ic_launcher) ?: error("Default icon not found")
        }
    }

    // 접근성 서비스 활성화 여부 확인
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals("${context.packageName}/com.example.guru2_cleanspirit.MyAccessibilityService", ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
