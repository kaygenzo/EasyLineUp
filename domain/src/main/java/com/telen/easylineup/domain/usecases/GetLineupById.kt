/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

class GetLineupById(private val dao: LineupRepository) :
    UseCase<GetLineupById.RequestValues, GetLineupById.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getLineupByIdSingle(requestValues.lineupId).map { ResponseValue(it) }
    }

    /**
     * @property lineup
     */
    class ResponseValue(val lineup: Lineup) : UseCase.ResponseValue

    /**
     * @property lineupId
     */
    class RequestValues(val lineupId: Long) : UseCase.RequestValues
}
