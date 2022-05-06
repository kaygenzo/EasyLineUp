package com.telen.easylineup.domain

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

internal object UseCaseHandler : KoinComponent {

    private val mainThreadScheduler: Scheduler by inject()

    fun <T : UseCase.RequestValues, R : UseCase.ResponseValue> execute(
        useCase: UseCase<T, R>, values: T,
        subscribeOn: Scheduler = Schedulers.io(),
        observeOn: Scheduler = mainThreadScheduler
    ): Single<R> {
        return useCase.executeUseCase(values).subscribeOn(subscribeOn).observeOn(observeOn)
    }
}