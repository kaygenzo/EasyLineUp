/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single
import java.lang.Exception

internal class DeleteLineup(private val lineupDao: LineupRepository) :
    UseCase<DeleteLineup.RequestValues, DeleteLineup.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupId?.let { id ->
            lineupDao.getLineupByIdSingle(id)
                .flatMapCompletable { lineup -> lineupDao.deleteLineup(lineup) }
                .andThen(Single.just(ResponseValue()))
        } ?: Single.error(Exception("Lineup id is null"))
    }

    /**
     * @property lineupId
     */
    class RequestValues(val lineupId: Long?) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
