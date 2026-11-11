/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import java.util.Calendar
import java.util.TimeZone

/*
 * Material3's DatePicker speaks in UTC midnights: it shows the UTC calendar day of the millis it
 * is given, and returns the UTC midnight of the day picked. The app stores and displays dates in
 * the device's time zone. Handing it stored millis as-is, or storing what it returns as-is,
 * shifts the day by one wherever local midnight is not UTC midnight - west of UTC, a picked
 * "Nov 10" came back as Nov 9 on screen. These two functions are the only crossing point.
 */

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** The UTC midnight of the calendar day [millis] falls on in [timeZone], for the picker. */
fun localTimeToPickerDate(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
    val local = Calendar.getInstance(timeZone).apply { timeInMillis = millis }
    return Calendar.getInstance(UTC).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** The midnight in [timeZone] of the calendar day the picker returned as [pickerDate]. */
fun pickerDateToLocalTime(pickerDate: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
    val utc = Calendar.getInstance(UTC).apply { timeInMillis = pickerDate }
    return Calendar.getInstance(timeZone).apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
