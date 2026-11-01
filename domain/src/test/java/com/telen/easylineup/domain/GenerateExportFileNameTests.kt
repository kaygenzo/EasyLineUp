/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.GenerateExportFileName
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class GenerateExportFileNameTests {
    private lateinit var generateExportFileName: GenerateExportFileName

    @Before
    fun init() {
        generateExportFileName = GenerateExportFileName()
    }

    @Test
    fun shouldEndWithTheEluExtension() {
        assertTrue(generateExportFileName(0L).endsWith(".elu"))
    }

    @Test
    fun shouldProduceDifferentNamesForDifferentTimestamps() {
        val first = generateExportFileName(0L)
        val second = generateExportFileName(86_400_000L)

        assertTrue(first != second)
    }
}
