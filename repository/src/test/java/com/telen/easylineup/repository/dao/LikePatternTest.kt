/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import org.junit.Assert.assertEquals
import org.junit.Test

class LikePatternTest {
    @Test
    fun plainTextIsUnchanged() {
        assertEquals("Fall Classic", "Fall Classic".escapeForLike())
    }

    @Test
    fun wildcardsAreEscaped() {
        assertEquals("100\\%", "100%".escapeForLike())
        assertEquals("u\\_12", "u_12".escapeForLike())
    }

    @Test
    fun theEscapeCharacterIsEscapedFirst() {
        assertEquals("a\\\\\\%", "a\\%".escapeForLike())
    }
}
