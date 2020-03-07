package com.telen.easylineup.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseAnalyticsUtils {
    fun logInvalidParameter(context: Context?, paramKey: String, paramValue: String, eventName: String) {
        context?.run {
            val params = Bundle()
            params.putString(paramKey, paramValue)
            FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
        }
    }
}