/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.IsExtraHittersUnlimited
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class IsExtraHittersUnlimitedTests {
    private lateinit var isExtraHittersUnlimited: IsExtraHittersUnlimited

    @Before
    fun init() {
        isExtraHittersUnlimited = IsExtraHittersUnlimited()
    }

    @Test
    fun shouldBeUnlimitedPastTheThreshold() {
        assertTrue(isExtraHittersUnlimited(Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED + 1))
    }

    @Test
    fun shouldNotBeUnlimitedAtOrBelowTheThreshold() {
        assertFalse(isExtraHittersUnlimited(Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED))
    }
}
