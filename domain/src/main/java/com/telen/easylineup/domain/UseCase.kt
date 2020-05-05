package com.telen.easylineup.domain

import io.reactivex.Single

internal abstract class UseCase<Q: UseCase.RequestValues, P: UseCase.ResponseValue> {

    interface RequestValues
    interface ResponseValue

    abstract fun executeUseCase(requestValues: Q): Single<P>
}