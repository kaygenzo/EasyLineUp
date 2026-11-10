/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Weekday, abbreviated month and day: "Tue, Nov 10" in English, "mar. 10 nov." in French. */
const val DATE_SKELETON_WEEKDAY_DAY_MONTH = "EEEMMMd"

/** Abbreviated month and day: "Nov 10" in English, "10 nov." in French. */
const val DATE_SKELETON_DAY_MONTH = "MMMd"

/** Abbreviated month, day and year: "Nov 10, 2026" in English, "10 nov. 2026" in French. */
const val DATE_SKELETON_DAY_MONTH_YEAR = "yMMMd"

/** Abbreviated month and year: "Nov 2026" in English, "nov. 2026" in French. */
const val DATE_SKELETON_MONTH_YEAR = "yMMM"

/**
 * Formats [millis] with the fields of [skeleton] laid out the way the current language writes
 * them: order, punctuation and words. A fixed pattern such as "EEE, MMM d" only translates the
 * month and weekday names and keeps the English order everywhere.
 */
fun formatLocalizedDate(
    millis: Long,
    skeleton: String,
    locale: Locale = Locale.getDefault()
): String {
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}
