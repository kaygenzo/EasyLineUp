/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

/**
 * Validates the team name, inserts or updates it, then marks it as the current team.
 *
 * @property dao
 */
class SaveTeam(
    val dao: TeamRepository,
    private val checkTeam: CheckTeam,
    private val saveCurrentTeam: SaveCurrentTeam
) : UseCase<SaveTeam.RequestValues, SaveTeam.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return checkTeam.executeUseCase(CheckTeam.RequestValues(requestValues.team))
            .flatMap {
                val team = requestValues.team
                if (team.type == TeamType.UNKNOWN.id) {
                    team.type = TeamType.BASEBALL.id
                }
                if (team.id == 0L) {
                    dao.insertTeam(team).map { id ->
                        team.id = id
                        team
                    }
                } else {
                    dao.updateTeam(team).andThen(Single.just(team))
                }
            }
            .flatMap { team ->
                saveCurrentTeam.executeUseCase(SaveCurrentTeam.RequestValues(team))
                    .map { ResponseValue(team) }
            }
    }

    /**
     * @property team
     */
    class ResponseValue(val team: Team) : UseCase.ResponseValue

    /**
     * @property team
     */
    class RequestValues(val team: Team) : UseCase.RequestValues
}
