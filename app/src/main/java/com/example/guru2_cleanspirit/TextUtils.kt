package com.example.guru2_cleanspirit

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun Context.getInstalledApps(): List<AppInfo> {
    val pm: PackageManager = packageManager
    val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    val appList = mutableListOf<AppInfo>()

    for (app in apps) {
        if (pm.getLaunchIntentForPackage(app.packageName) != null) {
            val appName = pm.getApplicationLabel(app).toString()
            appList.add(AppInfo(appName, app.packageName))
        }
    }
    return appList
}
