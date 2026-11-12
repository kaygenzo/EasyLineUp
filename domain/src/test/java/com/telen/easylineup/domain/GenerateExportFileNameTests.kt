/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.GenerateExportFileName
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GenerateExportFileNameTests {
    @Test
    fun shouldEndWithTheEluExtension() {
        assertTrue(GenerateExportFileName(fixedClock(0L))().endsWith(".elu"))
    }

    @Test
    fun shouldProduceDifferentNamesForDifferentTimestamps() {
        val first = GenerateExportFileName(fixedClock(0L))()
        val second = GenerateExportFileName(fixedClock(86_400_000L))()

        assertTrue(first != second)
    }

    @Test
    fun shouldStampTheClocksTime() {
        val name = GenerateExportFileName(fixedClock(86_400_000L))()

        // In the device's time zone, with its offset: a day after the epoch, give or take one.
        assertTrue(name, Regex("""1970-01-0[123]T\d{2}:\d{2}:\d{2}[+-]\d{4}\.elu""").matches(name))
    }
}
