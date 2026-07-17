/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

class InsertTeam(private val dao: TeamRepository) :
    UseCase<InsertTeam.RequestValues, InsertTeam.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.insertTeam(requestValues.team).map { ResponseValue(it) }
    }

    class RequestValues(val team: Team) : UseCase.RequestValues
    class ResponseValue(val id: Long) : UseCase.ResponseValue
}
