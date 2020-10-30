package com.telen.easylineup.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseAnalyticsUtils {

    const val EVENT_NAME_EXCEPTION = "elu_exception"
    const val KEY_EMPTY_FIELD = "empty_field"
    const val KEY_INVALID_FORMAT = "invalid_format"

    const val EVENT_MISSING_PITCHER = "elu_exception_missing_pitcher"
    const val EVENT_MISSING_DP_FLEX = "elu_exception_missing_dp_flex"

    const val EVENT_IMPORT_DATA = "elu_import_data"
    const val EVENT_EXPORT_DATA = "elu_export_data"
    const val EVENT_DELETE_DATA = "elu_delete_data"

    const val KEY_FIRST_TEAM = "first_team"

    private fun logInvalidParameter(context: Context?, eventName: String) {
        context?.run {
            FirebaseAnalytics.getInstance(this).logEvent(eventName, null)
        }
    }

    private fun logInvalidParameter(context: Context?, paramKey: String, paramValue: String, eventName: String) {
        context?.run {
            val params = Bundle()
            params.putString(paramKey, paramValue)
            FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
        }
    }

    private fun logInvalidParameter(context: Context?, paramKey: String, paramValue: Boolean, eventName: String) {
        context?.run {
            val params = Bundle()
            params.putBoolean(paramKey, paramValue)
            FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
        }
    }

    fun emptyTeamName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "team_name", EVENT_NAME_EXCEPTION)
    }

    fun emptyPlayerName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "player_name", EVENT_NAME_EXCEPTION)
    }

    fun invalidPlayerEmail(context: Context?) {
        logInvalidParameter(context, KEY_INVALID_FORMAT, "player_email", EVENT_NAME_EXCEPTION)
    }

    fun invalidPlayerPhoneNumber(context: Context?) {
        logInvalidParameter(context, KEY_INVALID_FORMAT, "player_phone_number", EVENT_NAME_EXCEPTION)
    }

    fun emptyPlayerID(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "player_id", EVENT_NAME_EXCEPTION)
    }

    fun emptyTournamentName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "tournament_name", EVENT_NAME_EXCEPTION)
    }

    fun emptyLineupName(context: Context?) {
        logInvalidParameter(context, KEY_EMPTY_FIELD, "lineup_name", EVENT_NAME_EXCEPTION)
    }

    fun missingPitcher(context: Context?) {
        logInvalidParameter(context, EVENT_MISSING_PITCHER)
    }

    fun missingDpFlex(context: Context?) {
        logInvalidParameter(context, EVENT_MISSING_DP_FLEX)
    }

    /* ACTIONS */

    fun importData(context: Context?) {
        logInvalidParameter(context, EVENT_IMPORT_DATA)
    }

    fun exportData(context: Context?) {
        logInvalidParameter(context, EVENT_EXPORT_DATA)
    }

    fun deleteData(context: Context?) {
        logInvalidParameter(context, EVENT_DELETE_DATA)
    }

    fun startTutorial(context: Context?, firstTeam: Boolean) {
        logInvalidParameter(context, KEY_FIRST_TEAM, firstTeam, FirebaseAnalytics.Event.TUTORIAL_BEGIN)
    }

    fun endTutorial(context: Context?) {
        logInvalidParameter(context, FirebaseAnalytics.Event.TUTORIAL_COMPLETE)
    }
}