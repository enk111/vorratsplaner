package com.example.enkkaufsliste.data

import android.content.Context

class Settings(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("einkaufsliste", Context.MODE_PRIVATE)

    var listId: String?
        get() = prefs.getString("listId", null)
        set(value) {
            prefs.edit().putString("listId", value).apply()
        }

    var cursor: Long
        get() = prefs.getLong("cursor", 0L)
        set(value) {
            prefs.edit().putLong("cursor", value).apply()
        }
}