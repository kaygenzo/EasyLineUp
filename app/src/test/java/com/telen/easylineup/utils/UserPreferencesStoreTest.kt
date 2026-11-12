/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class UserPreferencesStoreTest {
    private val context: Application = ApplicationProvider.getApplicationContext()
    private val store = SharedPreferencesUserPreferencesStore(context)

    @Test
    fun readsTheDefaultsWhenNothingIsStored() {
        val preferences = store.read()

        assertEquals(ThemeMode.FOLLOW_SYSTEM, preferences.themeMode)
        assertEquals(
            ContextCompat.getColor(context, R.color.default_men_color),
            preferences.menColor
        )
        assertEquals(
            ContextCompat.getColor(context, R.color.default_women_color),
            preferences.womenColor
        )
        assertFalse(preferences.importOverwriteExisting)
    }

    @Test
    fun readsBackWhatWasWritten() {
        store.setThemeMode(ThemeMode.DARK)
        store.setMenColor(0x112233)
        store.setWomenColor(0x445566)
        store.setImportOverwriteExisting(true)

        val preferences = SharedPreferencesUserPreferencesStore(context).read()

        assertEquals(ThemeMode.DARK, preferences.themeMode)
        assertEquals(0x112233, preferences.menColor)
        assertEquals(0x445566, preferences.womenColor)
        assertTrue(preferences.importOverwriteExisting)
    }

    @Test
    fun keepsTheKeysEarlierVersionsWrote() {
        // A user upgrading keeps their settings: same file, same keys, same value format.
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(context.getString(R.string.key_day_night_theme), "1")
            putInt(context.getString(R.string.key_women_style), 0x778899)
        }

        val preferences = store.read()

        assertEquals(ThemeMode.LIGHT, preferences.themeMode)
        assertEquals(0x778899, preferences.womenColor)
    }

    @Test
    fun anInvalidStoredThemeFollowsTheSystem() {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(context.getString(R.string.key_day_night_theme), "night")
        }

        assertEquals(ThemeMode.FOLLOW_SYSTEM, store.read().themeMode)
    }

    @Test
    fun sexColorsComeFromThePreferences() {
        store.setMenColor(0x0000FF)
        store.setWomenColor(0xFF0000)

        assertEquals(0x0000FF, Sex.MALE.getColor(context))
        assertEquals(0xFF0000, Sex.FEMALE.getColor(context))
        assertEquals(android.graphics.Color.TRANSPARENT, Sex.UNKNOWN.getColor(context))
    }
}
