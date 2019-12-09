package com.telen.easylineup.team

import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetPlayers
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Player
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class TeamViewModel: ViewModel(), KoinComponent {

    private val getTeamUseCase: GetTeam by inject()
    private val getPlayersUseCase: GetPlayers by inject()

    fun getPlayers(): Single<List<Player>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team -> UseCaseHandler.execute(getPlayersUseCase, GetPlayers.RequestValues(team.id)).map { it.players } }
    }
}