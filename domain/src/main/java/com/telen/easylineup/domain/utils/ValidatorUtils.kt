/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.utils

import com.telen.easylineup.domain.ports.PhoneNumberValidator
import java.util.regex.Matcher
import java.util.regex.Pattern

open class ValidatorUtils(private val phoneNumberValidator: PhoneNumberValidator) {
    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    open fun isEmailValid(email: String?): Boolean {
        return email.takeIf { !it.isNullOrEmpty() }?.let {
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher: Matcher = pattern.matcher(email)
            matcher.matches()
        } ?: true
    }

    open fun isValidPhoneNumber(phone: String?): Boolean {
        return phone.takeIf { !it.isNullOrEmpty() }?.let {
            phoneNumberValidator.isGlobalPhoneNumber(it)
        } ?: true
    }
}
