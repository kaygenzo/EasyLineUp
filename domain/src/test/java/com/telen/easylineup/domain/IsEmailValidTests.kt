/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.IsEmailValid
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class IsEmailValidTests {
    lateinit var isEmailValid: IsEmailValid

    @Before
    fun init() {
        isEmailValid = IsEmailValid()
    }

    @Test
    fun shouldAcceptNullEmail() {
        assertTrue(isEmailValid(null))
    }

    @Test
    fun shouldAcceptEmptyEmail() {
        assertTrue(isEmailValid(""))
    }

    @Test
    fun shouldAcceptValidEmail() {
        assertTrue(isEmailValid("player@easylineup.com"))
    }

    @Test
    fun shouldAcceptValidEmailRegardlessOfCase() {
        assertTrue(isEmailValid("Player@EasyLineup.COM"))
    }

    @Test
    fun shouldRejectEmailWithoutAt() {
        assertFalse(isEmailValid("player.easylineup.com"))
    }

    @Test
    fun shouldRejectEmailWithoutDomain() {
        assertFalse(isEmailValid("player@"))
    }

    @Test
    fun shouldRejectEmailWithoutTld() {
        assertFalse(isEmailValid("player@easylineup"))
    }

    @Test
    fun shouldRejectEmailWithSpaces() {
        assertFalse(isEmailValid("player @easylineup.com"))
    }
}
