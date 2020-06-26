package com.example.forvenders.base

import android.content.Context
import android.content.SharedPreferences

object PreferanceRepository {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun init(context: AppClass) {
        sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun setString(key: String, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String = sharedPreferences.getString(key, "")!!

    fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean = sharedPreferences.getBoolean(key, false)


    fun logout() {
        editor.clear()
        editor.apply()
    }
}