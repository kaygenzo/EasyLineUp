package com.telen.easylineup.domain.ports

interface PhoneNumberValidator {
    fun isGlobalPhoneNumber(phone: String): Boolean
}
