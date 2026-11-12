/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * The day/night theme the user picked in the settings.
 *
 * @property preferenceValue how the choice is stored in the preferences (`pref_theme_style_values`)
 */
enum class ThemeMode(val preferenceValue: String) {
    FOLLOW_SYSTEM("0"),
    LIGHT("1"),
    DARK("2"),
    ;

    companion object {
        /** A missing or unreadable stored value falls back to following the system theme. */
        fun fromPreference(value: String?): ThemeMode =
            entries.firstOrNull { it.preferenceValue == value?.trim() } ?: FOLLOW_SYSTEM
    }
}
