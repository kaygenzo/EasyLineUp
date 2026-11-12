/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ThemeModeTests {
    @Test
    fun shouldReadEveryStoredValue() {
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromPreference("0"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromPreference("1"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromPreference("2"))
    }

    @Test
    fun shouldRoundTripThroughThePreferenceValue() {
        ThemeMode.entries.forEach {
            assertEquals(it, ThemeMode.fromPreference(it.preferenceValue))
        }
    }

    @Test
    fun shouldFollowTheSystemWhenTheStoredValueIsInvalid() {
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromPreference(null))
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromPreference(""))
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromPreference("dark"))
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromPreference("7"))
    }
}
