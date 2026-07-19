/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.InsertLineups
import com.telen.easylineup.domain.usecases.InsertPlayerFieldPositions
import com.telen.easylineup.domain.usecases.InsertPlayerNumberOverlays
import com.telen.easylineup.domain.usecases.InsertPlayers
import com.telen.easylineup.domain.usecases.InsertTeam
import com.telen.easylineup.domain.usecases.InsertTournaments
import com.telen.easylineup.mock.DatabaseMockProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class EasyLineupActivityTestRule<T : Activity?> : ActivityTestRule<T>, KoinComponent {
    private val deleteAllDataUseCase: DeleteAllData by inject()
    private val databaseMockProvider: DatabaseMockProvider by lazy {
        DatabaseMockProvider(
            insertTeamUseCase = get<InsertTeam>(),
            insertPlayersUseCase = get<InsertPlayers>(),
            insertPlayerNumberOverlaysUseCase = get<InsertPlayerNumberOverlays>(),
            insertLineupsUseCase = get<InsertLineups>(),
            insertPlayerFieldPositionsUseCase = get<InsertPlayerFieldPositions>(),
            insertTournamentsUseCase = get<InsertTournaments>()
        )
    }
    private val context: Context by inject()

    constructor(activityClass: Class<T>?) : super(activityClass)
    constructor(activityClass: Class<T>?, initialTouchMode: Boolean, launchActivity: Boolean) : super(activityClass,
        initialTouchMode, launchActivity)

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()

        // activity?.applicationContext?.run {
        deleteAllDataUseCase()
            .andThen(databaseMockProvider.createMockDatabase(context))
            // .andThen(Completable.timer(3, TimeUnit.SECONDS))
            .blockingAwait()
        // }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesEditor = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0).edit()
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROSTER, false)
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM, false)
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON, false)
        preferencesEditor.putBoolean(Constants.PREF_FEATURE_SHOW_REORDER_HELP, false)
        preferencesEditor.commit()
    }
}
