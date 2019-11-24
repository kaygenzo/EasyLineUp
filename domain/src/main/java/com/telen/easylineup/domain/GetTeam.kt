package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.Team
import com.telen.easylineup.repository.data.TeamDao
import io.reactivex.Single

class GetTeam(val dao: TeamDao): UseCase<GetTeam.RequestValues, GetTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsList().map { it.first() }.map { ResponseValue(it) }
    }


    inner class ResponseValue(val team: Team): UseCase.ResponseValue
    inner class RequestValues(val teamID: Long): UseCase.RequestValues
}