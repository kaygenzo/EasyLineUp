/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import io.reactivex.rxjava3.core.Single

internal class SetLineupMode :
    UseCase<SetLineupMode.RequestValues, SetLineupMode.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineup.let { lineup ->
            lineup.mode = requestValues.lineupMode
            Single.just(ResponseValue())
        }
    }

    /**
     * @property lineup
     * @property lineupMode
     */
    class RequestValues(val lineup: Lineup, val lineupMode: Int) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
