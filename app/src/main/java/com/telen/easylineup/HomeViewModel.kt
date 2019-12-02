package com.telen.easylineup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Team
import io.reactivex.Single

class HomeViewModel: ViewModel() {

    private val getTeamUseCase = GetTeam(App.database.teamDao(), App.prefs)

    fun registerTeamUpdates(): LiveData<List<Team>> {
        return App.database.teamDao().getTeams()
    }

    fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map {
            it.team
        }
    }
}