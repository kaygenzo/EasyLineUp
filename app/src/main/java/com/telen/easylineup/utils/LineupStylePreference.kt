/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import com.telen.easylineup.R

/**
 * Reads the lineup font style Settings manages, behind Android's own SharedPreferences API so
 * [com.telen.easylineup.lineup.LineupViewModel] stays constructor-testable like every other
 * ViewModel in the app, instead of taking a raw [Context] to read it directly.
 */
interface LineupStylePreference {
    fun read(): String
}

class LineupStylePreferenceImpl(private val context: Context) : LineupStylePreference {
    override fun read(): String = SharedPreferencesUtils.getStringSetting(
        context,
        R.string.key_lineup_style,
        context.getString(R.string.lineup_style_default_value)
    )
}
