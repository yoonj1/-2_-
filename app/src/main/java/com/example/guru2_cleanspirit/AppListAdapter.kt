package com.example.guru2_cleanspirit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.Switch
import android.widget.TextView

class AppListAdapter(private val appList: List<AppInfo>, private val context: Context) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appName.text = appInfo.name
        holder.appSwitch.isChecked = isAppBlocked(appInfo.packageName)

        holder.appSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                blockApp(appInfo.packageName)
            } else {
                unblockApp(appInfo.packageName)
            }
        }
    }

    override fun getItemCount() = appList.size

    private fun isAppBlocked(packageName: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(packageName, false)
    }

    private fun blockApp(packageName: String) {
        val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(packageName, true)
        editor.apply()
    }

    private fun unblockApp(packageName: String) {
        val sharedPreferences = context.getSharedPreferences("AppBlockerPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(packageName, false)
        editor.apply()
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appNameTextView)
        val appSwitch: Switch = view.findViewById(R.id.toggleSwitch)
    }
}
