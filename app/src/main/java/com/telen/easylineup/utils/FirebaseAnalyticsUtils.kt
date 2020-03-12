package com.telen.easylineup.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseAnalyticsUtils {

    const val EVENT_NAME_EXCEPTION = "elu_exception"
    const val KEY_EMPTY_FIELD = "empty_field"

    fun logInvalidParameter(context: Context?, paramKey: String, paramValue: String, eventName: String) {
        context?.run {
            val params = Bundle()
            params.putString(paramKey, paramValue)
            FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
        }
    }

    fun emptyTeamName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "team_name", EVENT_NAME_EXCEPTION)
    }

    fun emptyPlayerName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "player_name", EVENT_NAME_EXCEPTION)
    }

    fun emptyPlayerLicense(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "player_license", EVENT_NAME_EXCEPTION)
    }

    fun emptyPlayerNumber(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "player_number", EVENT_NAME_EXCEPTION)
    }

    fun emptyTournamentName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "tournament_name", EVENT_NAME_EXCEPTION)
    }

    fun emptyLineupName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "lineup_name", EVENT_NAME_EXCEPTION)
    }
}