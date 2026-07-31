/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.ports.PhoneNumberValidator
import com.telen.easylineup.domain.usecases.IsValidPhoneNumberUseCase
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
internal class IsValidPhoneNumberUseCaseTests {
    @Mock
    lateinit var phoneNumberValidator: PhoneNumberValidator
    lateinit var isValidPhoneNumber: IsValidPhoneNumberUseCase

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        isValidPhoneNumber = IsValidPhoneNumberUseCase(phoneNumberValidator)
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
