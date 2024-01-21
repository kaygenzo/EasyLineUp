/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import io.reactivex.rxjava3.core.Single

internal abstract class UseCase<Q : UseCase.RequestValues, P : UseCase.ResponseValue> {
    abstract fun executeUseCase(requestValues: Q): Single<P>

    interface RequestValues
    interface ResponseValue
}
