/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import androidx.appcompat.view.ActionMode
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeamEmails
import com.telen.easylineup.domain.usecases.GetTeamPhones
import com.telen.easylineup.domain.usecases.ObserveTeams
import com.telen.easylineup.domain.usecases.SaveDashboardTiles
import com.telen.easylineup.utils.SharedPreferencesHelper
import com.telen.easylineup.utils.toLiveData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
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
    private val disposables = CompositeDisposable()
    var actionMode: ActionMode? = null

    fun registerTilesLiveData() = observeTeams().toLiveData().switchMap {
        getDashboardTilesLiveData()
    }

    private fun getDashboardTilesLiveData(): LiveData<List<DashboardTile>> {
        val resultLiveData: MutableLiveData<List<DashboardTile>> = MutableLiveData()
        val disposable = getDashboardTilesUseCase()
            .subscribe({
                resultLiveData.postValue(it)
            }, {
                Timber.e(it)
            })
        disposables.add(disposable)
        return resultLiveData
    }

    fun clear() {
        disposables.clear()
    }

    fun showNewReportIssueButtonFeature(): Single<Boolean> {
        val show = prefsHelper.isFeatureEnabled(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON)
        if (show) {
            prefsHelper.disableFeature(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON)
        }
        return Single.just(show)
    }

    fun saveTiles(tiles: List<DashboardTile>) = saveDashboardTilesUseCase(tiles)

    fun getShirtNumberHistory(number: Int) = getShirtNumberHistoryUseCase(number)

    fun getEmails() = getTeamEmailsUseCase()

    fun getPhones() = getTeamPhonesUseCase()
}
