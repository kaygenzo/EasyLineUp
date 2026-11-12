/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import android.graphics.Color
import com.telen.easylineup.domain.model.Sex

fun Sex.getColor(context: Context): Int =
    getColor(SharedPreferencesUserPreferencesStore(context).read())

/** The color the user picked for this sex in the settings, transparent when unknown. */
fun Sex.getColor(preferences: UserPreferences): Int = when (this) {
    Sex.MALE -> preferences.menColor
    Sex.FEMALE -> preferences.womenColor
    else -> Color.TRANSPARENT
}
