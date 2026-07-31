/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import com.telen.easylineup.domain.ports.PhoneNumberValidator
import com.telen.easylineup.domain.usecases.IsValidPhoneNumber
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class IsValidPhoneNumberTests {
    @Mock
    lateinit var phoneNumberValidator: PhoneNumberValidator
    lateinit var isValidPhoneNumber: IsValidPhoneNumber

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        isValidPhoneNumber = IsValidPhoneNumber(phoneNumberValidator)
    }

    @Test
    fun shouldAcceptNullPhoneWithoutDelegating() {
        assertTrue(isValidPhoneNumber(null))
        verify(phoneNumberValidator, never()).isGlobalPhoneNumber(any())
    }

    @Test
    fun shouldAcceptEmptyPhoneWithoutDelegating() {
        assertTrue(isValidPhoneNumber(""))
        verify(phoneNumberValidator, never()).isGlobalPhoneNumber(any())
    }

    @Test
    fun shouldDelegateToPhoneNumberValidatorWhenPhoneIsNotBlank() {
        Mockito.`when`(phoneNumberValidator.isGlobalPhoneNumber("+33612345678")).thenReturn(true)

        assertTrue(isValidPhoneNumber("+33612345678"))
    }

    @Test
    fun shouldRejectPhoneWhenPhoneNumberValidatorRejectsIt() {
        Mockito.`when`(phoneNumberValidator.isGlobalPhoneNumber("not-a-phone")).thenReturn(false)

        assertFalse(isValidPhoneNumber("not-a-phone"))
    }
}
