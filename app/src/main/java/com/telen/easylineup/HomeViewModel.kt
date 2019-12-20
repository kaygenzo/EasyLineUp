package com.telen.easylineup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.application.App
import com.telen.easylineup.domain.GetAllTeams
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.SaveCurrentTeam
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Team
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeViewModel: ViewModel(), KoinComponent {

    private val getTeamUseCase: GetTeam by inject()
    private val getAllTeamsUseCase: GetAllTeams by inject()
    private val saveCurrentTeam: SaveCurrentTeam by inject()

    fun registerTeamUpdates(): LiveData<List<Team>> {
        return App.database.teamDao().getTeams()
    }

    fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map {
            it.team
        }
    }

    fun onSwapButtonClicked(): Single<List<Team>> {
        return UseCaseHandler.execute(getAllTeamsUseCase, GetAllTeams.RequestValues()).map { it.teams }
    }

    fun updateCurrentTeam(currentTeam: Team): Completable {
        return UseCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(currentTeam)).ignoreElement()
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