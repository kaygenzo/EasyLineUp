/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

internal class UpdateLineup(private val lineupRepo: LineupRepository) :
    UseCase<UpdateLineup.RequestValues, UpdateLineup.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineup.let { lineup ->
            lineupRepo.updateLineup(lineup)
                .andThen(Single.just(ResponseValue()))
        }
    }

    /**
     * @property lineup
     */
    class RequestValues(val lineup: Lineup) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
