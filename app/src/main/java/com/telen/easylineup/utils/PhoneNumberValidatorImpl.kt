package com.telen.easylineup.utils

import android.telephony.PhoneNumberUtils
import com.telen.easylineup.domain.usecases.PhoneNumberValidator

class PhoneNumberValidatorImpl : PhoneNumberValidator {
    override fun isGlobalPhoneNumber(phone: String): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(phone)
    }
}
