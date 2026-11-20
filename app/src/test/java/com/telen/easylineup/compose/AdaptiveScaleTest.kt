/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveScaleTest {
    @Test
    fun theReferencePhoneKeepsItsSizes() {
        assertEquals(1f, adaptiveScale(411), 0.001f)
    }

    @Test
    fun aSmallPhoneShrinksALittleButNoLowerThanTheFloor() {
        assertEquals(0.88f, adaptiveScale(360), 0.001f)
        assertEquals(0.88f, adaptiveScale(320), 0.001f)
    }

    @Test
    fun aTabletGrowsUpToTheCeiling() {
        assertEquals(1.3f, adaptiveScale(800), 0.001f)
        assertEquals(1.3f, adaptiveScale(1200), 0.001f)
    }

    @Test
    fun inBetweenItFollowsTheSmallestWidth() {
        assertEquals(480f / 411f, adaptiveScale(480), 0.001f)
    }
}
