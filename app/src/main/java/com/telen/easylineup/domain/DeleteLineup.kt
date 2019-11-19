package com.telen.easylineup.domain

import com.telen.easylineup.UseCase
import com.telen.easylineup.data.LineupDao

class DeleteLineup(private val lineupDao: LineupDao): UseCase<DeleteLineup.RequestValues, DeleteLineup.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { req ->
            req.lineupID?.let { id ->
                lineupDao.getLineupByIdSingle(id)
                        .flatMapCompletable { lineup -> lineupDao.deleteLineup(lineup) }
                        .subscribe({
                            mUseCaseCallback?.onSuccess(ResponseValue())
                        }, {
                            mUseCaseCallback?.onError()
                        })
            } ?: mUseCaseCallback?.onError()
        }
    }

    class RequestValues(val lineupID: Long?): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}