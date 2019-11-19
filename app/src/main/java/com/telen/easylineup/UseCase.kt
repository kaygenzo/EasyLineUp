package com.telen.easylineup

abstract class UseCase<Q: UseCase.RequestValues, P: UseCase.ResponseValue> {

    interface RequestValues
    interface ResponseValue

    interface UseCaseCallback<R> {
        fun onSuccess(response: R)
        fun onError()
    }

    var mRequestValues: Q? = null
    var mUseCaseCallback: UseCaseCallback<P>? = null

    fun run() {
        executeUseCase(mRequestValues)
    }

    protected abstract fun executeUseCase(requestValues: Q?)
}