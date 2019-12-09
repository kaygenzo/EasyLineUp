package com.telen.easylineup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.GetAllTeams
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.SaveCurrentTeam
import com.telen.easylineup.repository.model.Team
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

class HomeViewModel: ViewModel() {

    private val getTeamUseCase = GetTeam(App.database.teamDao())
    private val getAllTeamsUseCase = GetAllTeams(App.database.teamDao())
    private val saveCurrentTeam = SaveCurrentTeam(App.database.teamDao())

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