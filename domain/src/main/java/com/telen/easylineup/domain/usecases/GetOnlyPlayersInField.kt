/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isSubstitute
import io.reactivex.rxjava3.core.Single

internal class GetOnlyPlayersInField :
    UseCase<GetOnlyPlayersInField.RequestValues, GetOnlyPlayersInField.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.playersInLineup)
            .map { list -> list.filter { it.position > 0 && !it.isSubstitute() && !it.isDpDh() } }
            .map { ResponseValue(it) }
    }

    /**
     * @property playersInField
     */
    class ResponseValue(val playersInField: List<PlayerWithPosition>) : UseCase.ResponseValue
    /**
     * @property playersInLineup
     */
    class RequestValues(val playersInLineup: List<PlayerWithPosition>) : UseCase.RequestValues
}
