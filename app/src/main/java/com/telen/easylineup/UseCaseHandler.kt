package com.telen.easylineup

import com.telen.easylineup.domain.UseCase
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object UseCaseHandler {

    fun <T : UseCase.RequestValues, R : UseCase.ResponseValue> execute(useCase: UseCase<T, R>, values: T,
                                                                                                                               subscribeOn: Scheduler = Schedulers.io(),
                                                                                                                               observeOn: Scheduler = AndroidSchedulers.mainThread()): Single<R> {
        return useCase.executeUseCase(values).subscribeOn(subscribeOn).observeOn(observeOn)
    }
}