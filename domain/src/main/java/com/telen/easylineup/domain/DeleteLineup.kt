package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.LineupDao
import io.reactivex.Single
import java.lang.Exception

class DeleteLineup(private val lineupDao: LineupDao): UseCase<DeleteLineup.RequestValues, DeleteLineup.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { id ->
            lineupDao.getLineupByIdSingle(id)
                    .flatMapCompletable { lineup -> lineupDao.deleteLineup(lineup) }
                    .andThen(Single.just(ResponseValue()))
        } ?: Single.error(Exception("Lineup id is null"))
    }

    class RequestValues(val lineupID: Long?): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}