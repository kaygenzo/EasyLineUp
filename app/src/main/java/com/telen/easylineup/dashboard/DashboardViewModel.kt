package com.telen.easylineup.dashboard

import android.content.Context
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.DashboardTile
import io.reactivex.rxjava3.core.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DashboardViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    var actionMode: ActionMode? = null

    fun registerTilesLiveData() = Transformations.switchMap(domain.teams().observeTeams()) {
        domain.data().getDashboardConfigurations()
    }

    fun showNewReportIssueButtonFeature(context: Context): Single<Boolean> {
        val prefs = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON, true)
        if (show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON, false).apply()
        }
        return Single.just(show)
    }

    fun saveTiles(tiles: List<DashboardTile>) = domain.data().updateDashboardConfiguration(tiles)
    fun getShirtNumberHistory(number: Int) = domain.players().getShirtNumberHistory(number)
    fun getEmails() = domain.players().getTeamEmails()
    fun getPhones() = domain.players().getTeamPhones()
}