package com.telen.easylineup.domain

import io.reactivex.rxjava3.core.Single

internal abstract class UseCase<Q : UseCase.RequestValues, P : UseCase.ResponseValue> {

    interface RequestValues
    interface ResponseValue

    abstract fun executeUseCase(requestValues: Q): Single<P>
}