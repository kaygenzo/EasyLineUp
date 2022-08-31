package com.telen.easylineup

import android.app.Activity
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EasyLineupActivityTestRule<T : Activity?> : ActivityTestRule<T>, KoinComponent {

    private val domain: ApplicationInteractor by inject()

    constructor(activityClass: Class<T>?) : super(activityClass)
    constructor(activityClass: Class<T>?, initialTouchMode: Boolean, launchActivity: Boolean) : super(activityClass, initialTouchMode, launchActivity)

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()

//        activity?.applicationContext?.run {
            domain.data().deleteAllData()
                    .andThen(domain.data().generateMockedData())
//                    .andThen(Completable.timer(3, TimeUnit.SECONDS))
                    .blockingAwait()
//        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesEditor = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0).edit()
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROSTER, false)
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM, false)
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON, false)
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_REORDER_HELP, false)
        preferencesEditor.commit()
    }
}
