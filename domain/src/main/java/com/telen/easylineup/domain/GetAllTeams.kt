package com.telen.easylineup.domain

import android.content.SharedPreferences
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Constants
import io.reactivex.Single

class GetAllTeams(val dao: TeamDao): UseCase<GetAllTeams.RequestValues, GetAllTeams.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsRx().map { ResponseValue(it) }
    }

    class ResponseValue(val teams: List<Team>): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}