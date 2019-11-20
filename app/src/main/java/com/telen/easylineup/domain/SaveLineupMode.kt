package com.telen.easylineup.domain

import com.telen.easylineup.UseCase
import com.telen.easylineup.data.LineupDao
import io.reactivex.Single
import java.lang.Exception

class SaveLineupMode(private val lineupDao: LineupDao): UseCase<SaveLineupMode.RequestValues, SaveLineupMode.ResponseValue>() {

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