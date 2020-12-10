package com.telen.easylineup.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

@SuppressLint("MissingPermission")
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

    private fun logEvent(context: Context?, eventName: String) {
        logEvent(context, null, eventName)
    }

    private fun logEvent(context: Context?, paramKey: String, paramValue: String, eventName: String) {
        val params = Bundle()
        params.putString(paramKey, paramValue)
        logEvent(context, params, eventName)
    }

    private fun logEvent(context: Context?, paramKey: String, paramValue: Boolean, eventName: String) {
        val params = Bundle()
        params.putBoolean(paramKey, paramValue)
        logEvent(context, params, eventName)
    }

    private fun logEvent(context: Context?, params: Bundle?, eventName: String) {
        context?.run {
            FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
        }
    }

    fun emptyTeamName(context: Context?) {
        logEvent(context, KEY_EMPTY_FIELD, "team_name", EVENT_NAME_EXCEPTION)
    }

    fun emptyPlayerName(context: Context?) {
        logEvent(context, KEY_EMPTY_FIELD, "player_name", EVENT_NAME_EXCEPTION)
    }

    fun invalidPlayerEmail(context: Context?) {
        logEvent(context, KEY_INVALID_FORMAT, "player_email", EVENT_NAME_EXCEPTION)
    }

    fun invalidPlayerPhoneNumber(context: Context?) {
        logEvent(context, KEY_INVALID_FORMAT, "player_phone_number", EVENT_NAME_EXCEPTION)
    }

    fun emptyPlayerID(context: Context?) {
        logEvent(context, KEY_EMPTY_FIELD, "player_id", EVENT_NAME_EXCEPTION)
    }

    fun emptyTournamentName(context: Context?) {
        logEvent(context, KEY_EMPTY_FIELD, "tournament_name", EVENT_NAME_EXCEPTION)
    }

    fun emptyLineupName(context: Context?) {
        logEvent(context, KEY_EMPTY_FIELD, "lineup_name", EVENT_NAME_EXCEPTION)
    }

    fun missingPitcher(context: Context?) {
        logEvent(context, EVENT_MISSING_PITCHER)
    }

    fun missingDpFlex(context: Context?) {
        logEvent(context, EVENT_MISSING_DP_FLEX)
    }

    /* EVENT */

    fun onClick(context: Context?, eventName: String) {
        logEvent(context, eventName)
    }

    /* ACTIONS */

    fun importData(context: Context?) {
        logEvent(context, EVENT_IMPORT_DATA)
    }

    fun exportData(context: Context?) {
        logEvent(context, EVENT_EXPORT_DATA)
    }

    fun deleteData(context: Context?) {
        logEvent(context, EVENT_DELETE_DATA)
    }

    fun startTutorial(context: Context?, firstTeam: Boolean) {
        logEvent(context, KEY_FIRST_TEAM, firstTeam, FirebaseAnalytics.Event.TUTORIAL_BEGIN)
    }

    fun endTutorial(context: Context?) {
        logEvent(context, FirebaseAnalytics.Event.TUTORIAL_COMPLETE)
    }

    fun onScreen(context: Context?, screenName: String) {
        context?.run {
            FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            })
        }
    }
}