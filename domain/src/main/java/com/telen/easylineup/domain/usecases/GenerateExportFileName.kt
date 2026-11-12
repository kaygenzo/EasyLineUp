/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.Clock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** The `.elu` export file's name: an ISO-8601-ish timestamp, so exports sort and never collide. */
class GenerateExportFileName(private val clock: Clock) {
    operator fun invoke(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
        return "${formatter.format(Date(clock.nowMillis()))}.elu"
    }
}
