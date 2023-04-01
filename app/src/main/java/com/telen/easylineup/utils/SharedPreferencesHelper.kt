package com.telen.easylineup.utils

import android.content.Context
import com.telen.easylineup.domain.Constants

class SharedPreferencesHelper(private val context: Context) {

    private val prefs =
        context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, Context.MODE_PRIVATE)

    fun disableFeature(key: String) {
        prefs.edit().putBoolean(key, false).apply()
    }

    fun isFeatureEnabled(key: String): Boolean {
        return prefs.getBoolean(key, true)
    }
}