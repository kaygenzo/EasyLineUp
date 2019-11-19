package com.telen.easylineup.domain

import com.telen.easylineup.UseCase
import com.telen.easylineup.data.LineupDao

class SaveLineupMode(private val lineupDao: LineupDao): UseCase<SaveLineupMode.RequestValues, SaveLineupMode.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { req ->
            req.lineupID?.let { id ->
                lineupDao.getLineupByIdSingle(id)
                        .flatMapCompletable { lineup ->
                            lineup.mode = req.lineupMode
                            lineupDao.updateLineup(lineup)
                        }
                        .subscribe({
                            mUseCaseCallback?.onSuccess(ResponseValue())
                        }, {
                            mUseCaseCallback?.onError()
                        })
            } ?: mUseCaseCallback?.onError()
        }
    }

    class RequestValues(val lineupID: Long?, val lineupMode: Int): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}