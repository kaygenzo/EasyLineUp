/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class GetTeam(val dao: TeamRepository) :
    UseCase<GetTeam.RequestValues, GetTeam.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsRx().map { teams -> teams.first { team -> team.main } }
            .map { ResponseValue(it) }
    }

    /**
     * @property team
     */
    class ResponseValue(val team: Team) : UseCase.ResponseValue
    class RequestValues : UseCase.RequestValues
}
