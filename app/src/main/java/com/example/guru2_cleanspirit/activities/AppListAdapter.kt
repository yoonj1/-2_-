package com.example.guru2_cleanspirit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.example.guru2_cleanspirit.services.AppBlockerService

class AppListAdapter(
    context: Context,
    private val appList: List<AppInfo>
) : ArrayAdapter<AppInfo>(context, 0, appList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        val appInfo = getItem(position)

        val appNameTextView = view.findViewById<TextView>(R.id.app_name_text_view)
        val icon = view.findViewById<ImageView>(R.id.icon)
        val toggleSwitch = view.findViewById<Switch>(R.id.app_switch)

        appNameTextView.text = appInfo?.name
        icon.setImageDrawable(appInfo?.icon)
        toggleSwitch.isChecked = appInfo?.packageName?.let { isAppBlocked(it) } ?: false
        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            appInfo?.packageName?.let { updateAppBlockStatus(it, isChecked) }
        }

        return view
    }

    private fun isAppBlocked(packageName: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(packageName, false)
    }

    private fun updateAppBlockStatus(packageName: String, isBlocked: Boolean) {
        val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(packageName, isBlocked)
            apply()
        }

        if (isBlocked) {
            AppBlockerService.blockApp(context, packageName)
        } else {
            AppBlockerService.unblockApp(context, packageName)
        }
    }
}
