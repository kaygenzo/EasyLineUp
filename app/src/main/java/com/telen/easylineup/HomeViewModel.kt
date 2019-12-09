package com.telen.easylineup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.application.App
import com.telen.easylineup.domain.GetAllTeams
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.SaveCurrentTeam
import com.telen.easylineup.repository.model.Team
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeViewModel: ViewModel(), KoinComponent {

    private val getTeamUseCase: GetTeam by inject()
    private val getAllTeamsUseCase: GetAllTeams by inject()
    private val saveCurrentTeam: SaveCurrentTeam by inject()

    private val swapTeamLiveData = MutableLiveData<List<Team>>()

    fun registerTeamUpdates(): LiveData<List<Team>> {
        return App.database.teamDao().getTeams()
    }

    fun registerTeamsDialog() : LiveData<List<Team>>{
        return swapTeamLiveData
    }

    fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map {
            it.team
        }
    }

    fun onSwapButtonClicked() {
        val disposable = UseCaseHandler.execute(getAllTeamsUseCase, GetAllTeams.RequestValues()).map { it.teams }
                .subscribe({
                    swapTeamLiveData.value = it
                }, {
                    Timber.e(it)
                })
    }

    fun updateCurrentTeam(currentTeam: Team): Completable {
        return UseCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(currentTeam)).ignoreElement()
    }

}