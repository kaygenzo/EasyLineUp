package com.telen.easylineup.domain

import com.telen.easylineup.UseCase
import com.telen.easylineup.data.Team
import com.telen.easylineup.data.TeamDao

class GetTeam(val dao: TeamDao): UseCase<GetTeam.RequestValues, GetTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {
            dao.getTeamsList().map { it.first() }.subscribe({
                mUseCaseCallback?.onSuccess(ResponseValue(it))
            }, {
                mUseCaseCallback?.onError()
            })
        }
    }


    inner class ResponseValue(val team: Team): UseCase.ResponseValue
    inner class RequestValues(val teamID: Long): UseCase.RequestValues
}