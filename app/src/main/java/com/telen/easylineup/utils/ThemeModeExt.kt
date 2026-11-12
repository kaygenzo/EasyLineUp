/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import androidx.appcompat.app.AppCompatDelegate
import com.telen.easylineup.domain.model.ThemeMode

/** Applies [this] to every Activity, recreating the visible ones when it changes the theme. */
fun ThemeMode.applyAsNightMode() {
    AppCompatDelegate.setDefaultNightMode(
        when (this) {
            ThemeMode.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
    )
}
