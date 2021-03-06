package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import io.reactivex.Single

internal class CheckTeam: UseCase<CheckTeam.RequestValues, CheckTeam.ResponseValue>() {

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