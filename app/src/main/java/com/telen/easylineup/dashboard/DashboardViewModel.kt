package com.telen.easylineup.dashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.ShirtNumberEntry
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

sealed class EventCase
data class GetTeamEmailsSuccess(val emails: List<String>): EventCase()
data class GetTeamPhonesSuccess(val phones: List<String>): EventCase()
object TeamEmailsEmpty: EventCase()
object TeamPhonesEmpty: EventCase()

const val INDEX_SEND_MESSAGES = 0
const val INDEX_SEND_EMAILS = 1
const val INDEX_SEND_OTHER = 2

class DashboardViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()

    val eventHandler = PublishSubject.create<EventCase>()
    val disposables = CompositeDisposable()

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

    fun getEmails() {
        val disposable = domain.getTeamEmails()
                .subscribe({
                    if(it.isNotEmpty())
                        eventHandler.onNext(GetTeamEmailsSuccess(it))
                    else
                        eventHandler.onNext(TeamEmailsEmpty)
                }, {
                    Timber.e(it)
                })
        this.disposables.add(disposable)
    }

    fun getPhones() {
        val disposable = domain.getTeamPhones()
                .subscribe({
                    if(it.isNotEmpty())
                        eventHandler.onNext(GetTeamPhonesSuccess(it))
                    else
                        eventHandler.onNext(TeamPhonesEmpty)
                }, {
                    Timber.e(it)
                })
        this.disposables.add(disposable)
    }
}