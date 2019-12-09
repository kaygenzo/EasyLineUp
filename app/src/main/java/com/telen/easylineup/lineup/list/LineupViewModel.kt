package com.telen.easylineup.lineup.list

import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.CreateLineup
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single

class LineupViewModel: ViewModel() {

    private val getTeamUseCase = GetTeam(App.database.teamDao())
    private val createLineupUseCase = CreateLineup(App.database.tournamentDao(), App.database.lineupDao())

    fun createNewLineup(tournament: Tournament, lineupTitle: String): Single<Long> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .flatMap { UseCaseHandler.execute(createLineupUseCase, CreateLineup.RequestValues(it.team.id, tournament, lineupTitle)) }
                .map { it.lineupID }
    }
}