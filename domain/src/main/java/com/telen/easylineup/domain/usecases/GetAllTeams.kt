package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

internal class GetAllTeams(val dao: TeamRepository): UseCase<GetAllTeams.RequestValues, GetAllTeams.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsRx().map { ResponseValue(it) }
    }

    class ResponseValue(val teams: List<Team>): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}