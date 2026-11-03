/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Flips the app's effective day/night theme for a test. Every Activity extends
 * [com.telen.easylineup.BaseActivity] (an `AppCompatActivity`), so changing the default night
 * mode recreates the Activity under test automatically, the same way Settings' own theme
 * switch does in production.
 */
object ThemeTestUtils {
    fun applyNightMode(mode: Int) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    fun resetNightMode() {
        applyNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
