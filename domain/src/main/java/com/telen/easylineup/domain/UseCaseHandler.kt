/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.SchedulersProvider
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single

internal open class UseCaseHandler(
    private val schedulersProvider: SchedulersProvider
) {
    open fun <T : UseCase.RequestValues, R : UseCase.ResponseValue> execute(
        useCase: UseCase<T, R>, values: T,
        subscribeOn: Scheduler = schedulersProvider.io(),
        observeOn: Scheduler = schedulersProvider.main()
    ): Single<R> {
        return useCase.executeUseCase(values).subscribeOn(subscribeOn).observeOn(observeOn)
    }
}
