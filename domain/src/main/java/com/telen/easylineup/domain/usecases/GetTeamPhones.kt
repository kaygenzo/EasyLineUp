/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import io.reactivex.rxjava3.core.Single

class GetTeamPhones(private val getPlayers: GetPlayers) :
    UseCase<GetTeamPhones.RequestValues, GetTeamPhones.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return getPlayers.executeUseCase(GetPlayers.RequestValues())
            .map { response ->
                response.players
                    .filter { !it.phone.isNullOrEmpty() }
                    .map { it.phone ?: "" }
            }
            .map { ResponseValue(it) }
    }

    class RequestValues : UseCase.RequestValues
    class ResponseValue(val phones: List<String>) : UseCase.ResponseValue
}
