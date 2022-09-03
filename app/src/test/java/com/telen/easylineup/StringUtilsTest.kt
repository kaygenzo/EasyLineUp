package com.telen.easylineup

import com.telen.easylineup.utils.StringUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StringUtilsTest {

    lateinit var stringUtils: StringUtils

    @Before
    fun setup() {
       stringUtils = StringUtils()
    }

    @Test
    fun shouldEmptyStringReturnQuestionMarksString() {
        assertEquals("?", stringUtils.nameToLetters(""))
        assertEquals("?", stringUtils.nameToLetters(" "))
        assertEquals("?", stringUtils.nameToLetters(" \n\r\n"))
    }

    @Test
    fun shouldUniqueStringReturn1Letter() {
        assertEquals(stringUtils.nameToLetters("Test"), "T")
    }

    @Test
    fun shouldComposedStringReturn2Letters() {
        assertEquals(stringUtils.nameToLetters("Test Blabla"), "TB")
        assertEquals(stringUtils.nameToLetters("Test-Blabla"), "TB")
    }

    @Test
    fun shouldNameBetweenSpacesReturnInitial() {
        assertEquals("L", stringUtils.nameToLetters("    Laura    "))
    }
}
