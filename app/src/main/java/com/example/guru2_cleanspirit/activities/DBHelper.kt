package com.example.guru2_cleanspirit.src

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = ("CREATE TABLE $TABLE_USERS ("
                + "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_USERNAME TEXT UNIQUE,"
                + "$COLUMN_PASSWORD TEXT)")
        val createBlockedAppsTable = ("CREATE TABLE $TABLE_BLOCKED_APPS ("
                + "$COLUMN_BLOCK_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_USER_ID INTEGER,"
                + "$COLUMN_PACKAGE_NAME TEXT,"
                + "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE)")

        db.execSQL(createUserTable)
        db.execSQL(createBlockedAppsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BLOCKED_APPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun insertUser(username: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null)
        val cursorCount = cursor.count
        cursor.close()
        db.close()
        return cursorCount > 0
    }

    private fun getUserId(username: String): Long {
        val db = this.readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_USERS, arrayOf(COLUMN_USER_ID), selection, selectionArgs, null, null, null)
        var userId = -1L
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
        }
        cursor.close()
        db.close()
        return userId
    }

    fun insertBlockedApp(username: String, packageName: String): Boolean {
        val userId = getUserId(username)
        if (userId == -1L) return false
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_PACKAGE_NAME, packageName)
        }
        val result = db.insert(TABLE_BLOCKED_APPS, null, values)
        db.close()
        return result != -1L
    }

    fun deleteBlockedApp(username: String, packageName: String): Boolean {
        val userId = getUserId(username)
        if (userId == -1L) return false
        val db = this.writableDatabase
        val selection = "$COLUMN_USER_ID = ? AND $COLUMN_PACKAGE_NAME = ?"
        val selectionArgs = arrayOf(userId.toString(), packageName)
        val result = db.delete(TABLE_BLOCKED_APPS, selection, selectionArgs)
        db.close()
        return result > 0
    }

    fun getBlockedApps(username: String): List<String> {
        val userId = getUserId(username)
        if (userId == -1L) return emptyList()
        val db = this.readableDatabase
        val selection = "$COLUMN_USER_ID = ?"
        val selectionArgs = arrayOf(userId.toString())
        val cursor = db.query(TABLE_BLOCKED_APPS, arrayOf(COLUMN_PACKAGE_NAME), selection, selectionArgs, null, null, null)
        val blockedApps = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            do {
                val packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME))
                blockedApps.add(packageName)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return blockedApps
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserDB.db"
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val TABLE_BLOCKED_APPS = "blocked_apps"
        private const val COLUMN_BLOCK_ID = "block_id"
        private const val COLUMN_PACKAGE_NAME = "package_name"
    }
}
