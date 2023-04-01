package com.telen.easylineup.dashboard

import androidx.appcompat.view.ActionMode
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.core.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DashboardViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    private val prefsHelper: SharedPreferencesHelper by inject()

    var actionMode: ActionMode? = null

    fun registerTilesLiveData() = Transformations.switchMap(domain.teams().observeTeams()) {
        domain.data().getDashboardConfigurations()
    }

    fun showNewReportIssueButtonFeature(): Single<Boolean> {
        val show = prefsHelper.isFeatureEnabled(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON)
        if (show) {
            prefsHelper.disableFeature(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON)
        }
        return Single.just(show)
    }

    fun saveTiles(tiles: List<DashboardTile>) = domain.data().updateDashboardConfiguration(tiles)
    fun getShirtNumberHistory(number: Int) = domain.players().getShirtNumberHistory(number)
    fun getEmails() = domain.players().getTeamEmails()
    fun getPhones() = domain.players().getTeamPhones()
}