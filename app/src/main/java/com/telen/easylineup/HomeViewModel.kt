package com.telen.easylineup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.Team
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    fun registerTeamUpdates(): LiveData<List<Team>> {
        return domain.observeTeams()
    }

    fun getTeam(): Single<Team> {
        return domain.getTeam()
    }

    private fun getAllTeams(): Single<List<Team>> {
        return domain.getAllTeams()
    }

    fun getTeamsCount(): Single<Int> {
        return domain.getTeamsCount()
    }

    fun onSwapButtonClicked(): Single<List<Team>> {
        return getAllTeams()
    }

    fun updateCurrentTeam(currentTeam: Team): Completable {
        return domain.updateCurrentTeam(currentTeam)
    }

    fun showNewSwapTeamFeature(context: Context): Single<Boolean> {
        val prefs = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM, true)
        if(show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM, false).apply()
        }
        return Single.just(show)
    }

}