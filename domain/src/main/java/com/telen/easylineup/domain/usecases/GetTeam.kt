package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.Single

internal class GetTeam(val dao: TeamRepository): UseCase<GetTeam.RequestValues, GetTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsRx().map { teams -> teams.first { team -> team.main } }.map { ResponseValue(it) }
    }

    class ResponseValue(val team: Team): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}