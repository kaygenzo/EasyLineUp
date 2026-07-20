/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.dashboard

import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeamEmails
import com.telen.easylineup.domain.usecases.GetTeamPhones
import com.telen.easylineup.domain.usecases.ObserveTeams
import com.telen.easylineup.domain.usecases.SaveDashboardTiles
import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class DashboardViewModel : ViewModel(), KoinComponent {
    private val observeTeams: ObserveTeams by inject()
    private val getDashboardTilesUseCase: GetDashboardTiles by inject()
    private val saveDashboardTilesUseCase: SaveDashboardTiles by inject()
    private val getShirtNumberHistoryUseCase: GetShirtNumberHistory by inject()
    private val getTeamEmailsUseCase: GetTeamEmails by inject()
    private val getTeamPhonesUseCase: GetTeamPhones by inject()
    private val prefsHelper: SharedPreferencesHelper by inject()
    var actionMode: ActionMode? = null

    fun registerTilesFlow(): Flow<List<DashboardTile>> = observeTeams().catch { Timber.e(it) }
        .flatMapLatest {
            getDashboardTilesFlow()
        }

    private fun getDashboardTilesFlow(): Flow<List<DashboardTile>> = flow {
        getDashboardTilesUseCase()
            .onSuccess { emit(it) }
            .onFailure { Timber.e(it) }
    }

    fun showNewReportIssueButtonFeature(): Single<Boolean> {
        val show = prefsHelper.isFeatureEnabled(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON)
        if (show) {
            prefsHelper.disableFeature(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON)
        }
        return Single.just(show)
    }

    suspend fun saveTiles(tiles: List<DashboardTile>) = saveDashboardTilesUseCase(tiles)

    suspend fun getShirtNumberHistory(number: Int) = getShirtNumberHistoryUseCase(number)

    suspend fun getEmails() = getTeamEmailsUseCase()

    suspend fun getPhones() = getTeamPhonesUseCase()
}
