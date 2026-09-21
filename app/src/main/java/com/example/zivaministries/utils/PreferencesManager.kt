package com.example.zivaministries.utils

import android.content.Context
import android.content.SharedPreferences


/*
* Manages saving and loading user preferences.
* Used to remember the last page a user was on.
* */
class PreferencesManager(context: Context) {

    // SharedPreferences file name
    private val prefs: SharedPreferences = context.getSharedPreferences("ziva_prefs", Context.MODE_PRIVATE)

    fun saveLastPage(magazineId: Int, pageNumber: Int) {
        prefs.edit().putInt("magazine_${magazineId}_page", pageNumber).apply()
    }

    fun getLastPage(magazineId: Int): Int {
        return prefs.getInt("magazine_${magazineId}_page", 0)
    }

    // Clear the saved page for a magazine (when user finishes reading or deletes it)

    fun clearLastPage(magazineId: Int) {
        prefs.edit().remove("magazine_${magazineId}_page").apply()
    }
}