package com.example.guru2_cleanspirit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private val context: Context,
    private val appList: List<AppInfo>,
    private val isTimerRunning: Boolean
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appNameTextView.text = appInfo.appName
        holder.icon.setImageDrawable(appInfo.icon)
        holder.toggleSwitch.isChecked = isAppBlocked(appInfo.packageName)
        holder.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isTimerRunning) {
                updateAppBlockStatus(appInfo.packageName, isChecked)
            }
        }
    }

    override fun getItemCount(): Int = appList.size

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameTextView: TextView = view.findViewById(R.id.app_name_text_view)
        val icon: ImageView = view.findViewById(R.id.icon)
        val toggleSwitch: Switch = view.findViewById(R.id.app_switch)
    }

    private fun isAppBlocked(packageName: String): Boolean {
        // 앱이 차단되어 있는지 여부를 확인하는 로직
        // SharedPreferences나 Database에서 해당 앱의 차단 상태를 확인
        val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(packageName, false)
    }

    private fun updateAppBlockStatus(packageName: String, isBlocked: Boolean) {
        // 앱 차단 상태를 업데이트하는 로직
        val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(packageName, isBlocked)
            apply()
        }

        if (isBlocked) {
            // 타이머가 작동 중일 때 앱을 차단하는 로직
            AppBlockerService.blockApp(context, packageName)
        } else {
            // 타이머가 작동 중일 때 앱 차단을 해제하는 로직
            AppBlockerService.unblockApp(context, packageName)
        }
    }
}
