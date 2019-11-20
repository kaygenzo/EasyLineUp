package com.telen.easylineup

import io.reactivex.Single

abstract class UseCase<Q: UseCase.RequestValues, P: UseCase.ResponseValue> {

    interface RequestValues
    interface ResponseValue

    abstract fun executeUseCase(requestValues: Q): Single<P>
}