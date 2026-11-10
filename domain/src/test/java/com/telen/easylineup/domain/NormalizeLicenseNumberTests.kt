/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.normalizeLicenseNumber
import org.junit.Assert.assertEquals
import org.junit.Test

internal class NormalizeLicenseNumberTests {
    @Test
    fun keepsAlphanumericLicenseNumbers() {
        assertEquals("FR-22841A", normalizeLicenseNumber("FR-22841A"))
    }

    @Test
    fun keepsPurelyNumericLicenseNumbers() {
        assertEquals("22841", normalizeLicenseNumber("22841"))
    }

    @Test
    fun trimsSurroundingSpaces() {
        assertEquals("AB12", normalizeLicenseNumber("  AB12 "))
    }

    @Test
    fun readsMissingOrBlankAsNone() {
        assertEquals("", normalizeLicenseNumber(null))
        assertEquals("", normalizeLicenseNumber("   "))
    }

    @Test
    fun readsTheLegacyZeroAsNone() {
        assertEquals("", normalizeLicenseNumber("0"))
    }
}
