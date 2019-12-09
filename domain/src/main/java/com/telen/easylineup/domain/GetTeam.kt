package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Team
import io.reactivex.Single

class GetTeam(val dao: TeamDao): UseCase<GetTeam.RequestValues, GetTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsRx().map { teams -> teams.first { team -> team.main } }.map { ResponseValue(it) }
    }

    class ResponseValue(val team: Team): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}