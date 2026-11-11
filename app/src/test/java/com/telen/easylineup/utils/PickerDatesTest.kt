/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class PickerDatesTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val newYork = TimeZone.getTimeZone("America/New_York")
    private val paris = TimeZone.getTimeZone("Europe/Paris")
    private val auckland = TimeZone.getTimeZone("Pacific/Auckland")

    private fun at(timeZone: TimeZone, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance(timeZone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, day, hour, minute)
        }.timeInMillis

    private fun dayOf(millis: Long, timeZone: TimeZone): Int =
        Calendar.getInstance(timeZone).apply { timeInMillis = millis }.get(Calendar.DAY_OF_MONTH)

    @Test
    fun aPickedDayStaysTheSameDayWestOfUtc() {
        // The picker hands back UTC midnight of Nov 10: Nov 9, 19:00 in New York.
        val picked = at(utc, 10)

        val stored = pickerDateToLocalTime(picked, newYork)

        assertEquals(at(newYork, 10), stored)
        assertEquals(10, dayOf(stored, newYork))
    }

    @Test
    fun aPickedDayStaysTheSameDayEastOfUtc() {
        val picked = at(utc, 10)

        assertEquals(at(auckland, 10), pickerDateToLocalTime(picked, auckland))
        assertEquals(at(paris, 10), pickerDateToLocalTime(picked, paris))
    }

    @Test
    fun theLateEveningOfALocalDayOpensThePickerOnThatDay() {
        // Nov 10, 23:30 in New York is already Nov 11 in UTC.
        val stored = at(newYork, 10, hour = 23, minute = 30)

        assertEquals(at(utc, 10), localTimeToPickerDate(stored, newYork))
    }

    @Test
    fun theEarlyMorningOfALocalDayOpensThePickerOnThatDay() {
        // Nov 10, 00:30 in Auckland is still Nov 9 in UTC.
        val stored = at(auckland, 10, hour = 0, minute = 30)

        assertEquals(at(utc, 10), localTimeToPickerDate(stored, auckland))
    }

    @Test
    fun aRoundTripKeepsTheCalendarDay() {
        listOf(newYork, paris, auckland, utc).forEach { timeZone ->
            val stored = at(timeZone, 10, hour = 15)

            val roundTrip = pickerDateToLocalTime(localTimeToPickerDate(stored, timeZone), timeZone)

            assertEquals(timeZone.id, at(timeZone, 10), roundTrip)
        }
    }
}
