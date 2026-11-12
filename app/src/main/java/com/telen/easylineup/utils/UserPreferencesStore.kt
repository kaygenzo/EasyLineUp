/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.ThemeMode

/** What the settings screen lets the user choose, as stored on the device. */
data class UserPreferences(
    val themeMode: ThemeMode,
    val menColor: Int,
    val womenColor: Int,
    val importOverwriteExisting: Boolean
)

/**
 * The user's settings, kept out of the ViewModels so they never need a [Context]. [read] can hit
 * the disk the first time the preferences file is opened: call it off the main thread. Writes are
 * applied asynchronously.
 */
interface UserPreferencesStore {
    fun read(): UserPreferences
    fun setThemeMode(mode: ThemeMode)
    fun setMenColor(color: Int)
    fun setWomenColor(color: Int)
    fun setImportOverwriteExisting(value: Boolean)
}

class SharedPreferencesUserPreferencesStore(
    private val context: Context
) : UserPreferencesStore {
    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun read() = UserPreferences(
        themeMode = ThemeMode.fromPreference(
            preferences.getString(key(R.string.key_day_night_theme), null)
        ),
        menColor = preferences.getInt(
            key(R.string.key_men_style),
            ContextCompat.getColor(context, R.color.default_men_color)
        ),
        womenColor = preferences.getInt(
            key(R.string.key_women_style),
            ContextCompat.getColor(context, R.color.default_women_color)
        ),
        importOverwriteExisting = preferences.getBoolean(
            key(R.string.key_import_data_update_object),
            false
        )
    )

    override fun setThemeMode(mode: ThemeMode) {
        preferences.edit { putString(key(R.string.key_day_night_theme), mode.preferenceValue) }
    }

    override fun setMenColor(color: Int) {
        preferences.edit { putInt(key(R.string.key_men_style), color) }
    }

    override fun setWomenColor(color: Int) {
        preferences.edit { putInt(key(R.string.key_women_style), color) }
    }

    override fun setImportOverwriteExisting(value: Boolean) {
        preferences.edit { putBoolean(key(R.string.key_import_data_update_object), value) }
    }

    private fun key(@StringRes keyRes: Int) = context.getString(keyRes)
}
