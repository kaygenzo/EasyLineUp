package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.Single
import java.lang.Exception

internal class SaveLineupMode(private val lineupDao: LineupRepository): UseCase<SaveLineupMode.RequestValues, SaveLineupMode.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { id ->
                lineupDao.getLineupByIdSingle(id)
                        .flatMapCompletable { lineup ->
                            lineup.mode = requestValues.lineupMode
                            lineupDao.updateLineup(lineup)
                        }.andThen(Single.just(ResponseValue()))
            } ?: Single.error(Exception("Lineup id is null!"))
    }

    class RequestValues(val lineupID: Long?, val lineupMode: Int): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}