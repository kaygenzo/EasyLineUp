package com.telen.easylineup.dashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.ShirtNumberEntry
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class DashboardViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    fun registerTilesLiveData(): LiveData<List<DashboardTile>> {
        return Transformations.switchMap(domain.observeTeams()) {
            domain.getTiles()
        }
    }

    fun saveTiles(tiles: List<DashboardTile>): Completable {
        return domain.updateTiles(tiles)
    }

    fun showNewReportIssueButtonFeature(context: Context): Single<Boolean> {
        val prefs = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON, true)
        if(show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON, false).apply()
        }
        return Single.just(show)
    }

    fun getShirtNumberHistory(number: Int): Single<List<ShirtNumberEntry>> {
        return domain.getShirtNumberHistory(number)
    }
}