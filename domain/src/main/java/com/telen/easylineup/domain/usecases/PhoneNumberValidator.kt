package com.telen.easylineup.domain.usecases

interface PhoneNumberValidator {
    fun isGlobalPhoneNumber(phone: String): Boolean
}
