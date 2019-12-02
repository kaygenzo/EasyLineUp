package com.telen.easylineup.domain

import android.content.SharedPreferences
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.data.TeamDao
import io.reactivex.Single

class GetTeam(val dao: TeamDao, val pref: SharedPreferences): UseCase<GetTeam.RequestValues, GetTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsRx().map { it.first() }.map { ResponseValue(it) }
    }

    class ResponseValue(val team: Team): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}