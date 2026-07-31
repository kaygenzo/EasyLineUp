/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import java.util.regex.Matcher
import java.util.regex.Pattern

class IsEmailValid {
    operator fun invoke(email: String?): Boolean {
        return email.takeIf { !it.isNullOrEmpty() }?.let {
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher: Matcher = pattern.matcher(it)
            matcher.matches()
        } ?: true
    }
}
