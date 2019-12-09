package com.telen.easylineup.team

import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetPlayers
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Player
import io.reactivex.Single

class TeamViewModel: ViewModel() {

    private val getTeamUseCase = GetTeam(App.database.teamDao())
    private val getPlayersUseCase = GetPlayers(App.database.playerDao())

    fun getPlayers(): Single<List<Player>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team -> UseCaseHandler.execute(getPlayersUseCase, GetPlayers.RequestValues(team.id)).map { it.players } }
    }
}