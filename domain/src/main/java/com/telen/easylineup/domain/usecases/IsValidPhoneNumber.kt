/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.PhoneNumberValidator

class IsValidPhoneNumber(private val phoneNumberValidator: PhoneNumberValidator) {
    operator fun invoke(phone: String?): Boolean {
        return phone.takeIf { !it.isNullOrEmpty() }?.let {
            phoneNumberValidator.isGlobalPhoneNumber(it)
        } ?: true
    }
}
