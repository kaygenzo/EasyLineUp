/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import io.reactivex.rxjava3.core.Single

class GetTeamEmails(private val getPlayers: GetPlayers) :
    UseCase<GetTeamEmails.RequestValues, GetTeamEmails.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return getPlayers.executeUseCase(GetPlayers.RequestValues())
            .map { response ->
                response.players
                    .filter { !it.email.isNullOrEmpty() }
                    .map { it.email ?: "" }
            }
            .map { ResponseValue(it) }
    }

    class RequestValues : UseCase.RequestValues
    class ResponseValue(val emails: List<String>) : UseCase.ResponseValue
}
