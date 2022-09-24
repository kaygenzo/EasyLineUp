package com.telen.easylineup.utils

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Sex

fun Sex.getColor(context: Context): Int {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    return when(this) {
        Sex.MALE -> {
            val key = context.getString(R.string.key_men_style)
            preferences.getInt(key, ContextCompat.getColor(context, defaultColorRes))
        }
        Sex.FEMALE -> {
            val key = context.getString(R.string.key_women_style)
            preferences.getInt(key, ContextCompat.getColor(context, defaultColorRes))
        }
        else -> Color.TRANSPARENT
    }
}