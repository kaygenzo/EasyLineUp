package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.Team
import io.reactivex.Single

class NameEmptyException: Exception()

class CheckTeam: UseCase<CheckTeam.RequestValues, CheckTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.team)
                .flatMap { team ->
                    if("" == team.name.trim()) {
                        Single.error(NameEmptyException())
                    }
                    else
                        Single.just(ResponseValue())
                }
    }

    class ResponseValue: UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}