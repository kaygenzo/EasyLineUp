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
 * @property dao
 */
internal class SaveTeam(val dao: TeamRepository) :
    UseCase<SaveTeam.RequestValues, SaveTeam.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.team)
            .flatMap { team ->
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
            .flatMap {
                Single.just(ResponseValue(it))
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
