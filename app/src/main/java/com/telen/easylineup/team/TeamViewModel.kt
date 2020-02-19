package com.telen.easylineup.team

import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.DeleteTeam
import com.telen.easylineup.domain.GetPlayers
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Team
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class TeamViewModel: ViewModel(), KoinComponent {

    private var playerSelectedID = 0L

    private val getTeamUseCase: GetTeam by inject()
    private val getPlayersUseCase: GetPlayers by inject()
    private val deleteTeamUseCase: DeleteTeam by inject()

    fun getPlayers(): Single<List<Player>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team -> UseCaseHandler.execute(getPlayersUseCase, GetPlayers.RequestValues(team.id)).map { it.players } }
    }

    fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
    }

    fun deleteTeam(team: Team): Completable {
        return UseCaseHandler.execute(deleteTeamUseCase, DeleteTeam.RequestValues(team)).ignoreElement()
    }

    fun getPlayerId() : Long {
        return playerSelectedID
    }

    fun setPlayerId(id: Long) {
        playerSelectedID = id
    }
}