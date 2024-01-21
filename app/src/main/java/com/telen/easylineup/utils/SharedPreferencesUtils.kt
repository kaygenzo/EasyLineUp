/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager

object SharedPreferencesUtils {
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getStringSetting(context: Context, @StringRes key: Int, default: String): String {
        val preferences = getSharedPreferences(context)
        val keyValue = context.getString(key)
        return preferences.getString(keyValue, default) ?: default
    }

    fun getIntSetting(context: Context, @StringRes key: Int, default: Int): Int {
        val preferences = getSharedPreferences(context)
        return preferences.getInt(context.getString(key), default)
    }
}
