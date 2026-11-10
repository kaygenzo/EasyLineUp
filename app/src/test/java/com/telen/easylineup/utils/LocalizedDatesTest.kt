/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.app.Application
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dates follow the language's own layout, not a fixed English pattern that only translates the
 * month and weekday names (the old "EEE, MMM d" printed "mar., nov. 10" in French).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LocalizedDatesTest {
    private val defaultTimeZone = TimeZone.getDefault()

    // Tuesday, November 10, 2026
    private val millis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(2026, Calendar.NOVEMBER, 10, 12, 0)
    }.timeInMillis

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun englishPutsTheMonthBeforeTheDay() {
        assertEquals(
            "Tue, Nov 10",
            formatLocalizedDate(millis, DATE_SKELETON_WEEKDAY_DAY_MONTH, Locale.US)
        )
        assertEquals("Nov 10", formatLocalizedDate(millis, DATE_SKELETON_DAY_MONTH, Locale.US))
        assertEquals(
            "Nov 10, 2026",
            formatLocalizedDate(millis, DATE_SKELETON_DAY_MONTH_YEAR, Locale.US)
        )
    }

    @Test
    fun frenchPutsTheDayBeforeTheMonth() {
        assertEquals(
            "mar. 10 nov.",
            formatLocalizedDate(millis, DATE_SKELETON_WEEKDAY_DAY_MONTH, Locale.FRANCE)
        )
        assertEquals(
            "10 nov. 2026",
            formatLocalizedDate(millis, DATE_SKELETON_DAY_MONTH_YEAR, Locale.FRANCE)
        )
    }

    @Test
    fun japaneseUsesItsOwnDateMarkers() {
        assertEquals(
            "11月10日",
            formatLocalizedDate(millis, DATE_SKELETON_DAY_MONTH, Locale.JAPAN)
        )
    }
}
