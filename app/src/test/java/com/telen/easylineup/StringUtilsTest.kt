package com.telen.easylineup

import com.telen.easylineup.utils.toLetters
import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun shouldEmptyStringReturnQuestionMarksString() {
        assertEquals("?", "".toLetters())
        assertEquals("?", " ".toLetters())
        assertEquals("?", " \n\r\n".toLetters())
    }

    @Test
    fun shouldUniqueStringReturn1Letter() {
        assertEquals("T", "Test".toLetters(), )
    }

    @Test
    fun shouldComposedStringReturn2Letters() {
        assertEquals("TB", "Test Blabla".toLetters(), )
        assertEquals("TB", "Test-Blabla".toLetters(), )
    }

    @Test
    fun shouldNameBetweenSpacesReturnInitial() {
        assertEquals("L", "    Laura    ".toLetters())
    }
}
