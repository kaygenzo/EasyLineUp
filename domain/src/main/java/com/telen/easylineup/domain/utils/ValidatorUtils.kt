package com.telen.easylineup.domain.utils

import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ValidatorUtils {

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    fun isEmailValid(email: String?): Boolean {
        return email.takeIf { !TextUtils.isEmpty(it) }?.let {
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher: Matcher = pattern.matcher(email)
            matcher.matches()
        } ?: true
    }

    fun isValidPhoneNumber(phone: String?): Boolean {
        return phone.takeIf { !TextUtils.isEmpty(it) }?.let {
            PhoneNumberUtils.isGlobalPhoneNumber(it)
        } ?: true
    }
}