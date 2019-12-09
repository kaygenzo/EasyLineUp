package com.telen.easylineup.lineup.list

import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.CreateLineup
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class LineupViewModel: ViewModel(), KoinComponent {

    private val getTeamUseCase: GetTeam by inject()
    private val createLineupUseCase: CreateLineup by inject()

    fun createNewLineup(tournament: Tournament, lineupTitle: String): Single<Long> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .flatMap { UseCaseHandler.execute(createLineupUseCase, CreateLineup.RequestValues(it.team.id, tournament, lineupTitle)) }
                .map { it.lineupID }
    }
}