package com.telen.easylineup

import android.app.Activity
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationPort
import org.koin.core.KoinComponent
import org.koin.core.inject

class EasyLineupActivityTestRule<T : Activity?> : ActivityTestRule<T>, KoinComponent {

    private val domain: ApplicationPort by inject()

    constructor(activityClass: Class<T>?) : super(activityClass)
    constructor(activityClass: Class<T>?, initialTouchMode: Boolean, launchActivity: Boolean) : super(activityClass, initialTouchMode, launchActivity)

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()

//        activity?.applicationContext?.run {
            domain.deleteAllData()
                    .andThen(domain.generateMockedData())
//                    .andThen(Completable.timer(3, TimeUnit.SECONDS))
                    .blockingAwait()
//        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesEditor = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0).edit()
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROSTER, false)
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM, false)
        preferencesEditor.commit()
    }
}