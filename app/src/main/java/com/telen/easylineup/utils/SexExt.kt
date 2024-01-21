/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Sex

fun Sex.getColor(context: Context): Int {
    return when (this) {
        Sex.MALE -> SharedPreferencesUtils.getIntSetting(
            context,
            R.string.key_men_style,
            ContextCompat.getColor(context, defaultColorRes)
        )

        Sex.FEMALE -> SharedPreferencesUtils.getIntSetting(
            context,
            R.string.key_women_style,
            ContextCompat.getColor(context, defaultColorRes)
        )

        else -> Color.TRANSPARENT
    }
}
