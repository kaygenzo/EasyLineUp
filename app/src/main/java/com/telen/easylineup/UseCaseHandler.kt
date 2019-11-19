package com.telen.easylineup

object UseCaseHandler {

    private var mUseCaseScheduler: UseCaseScheduler = UseCaseThreadPoolScheduler()

    private class UiCallbackWrapper<V : UseCase.ResponseValue>(private val mCallback: UseCase.UseCaseCallback<V>,
                                                               private val mUseCaseHandler: UseCaseHandler) : UseCase.UseCaseCallback<V> {

        override fun onSuccess(response: V) {
            mUseCaseHandler.notifyResponse(response, mCallback)
        }

        override fun onError() {
            mUseCaseHandler.notifyError(mCallback)
        }
    }

    fun <V : UseCase.ResponseValue> notifyResponse(response: V, useCaseCallback: UseCase.UseCaseCallback<V>) {
        mUseCaseScheduler.notifyResponse(response, useCaseCallback)
    }

    private fun <V : UseCase.ResponseValue> notifyError(useCaseCallback: UseCase.UseCaseCallback<V>) {
        mUseCaseScheduler.onError(useCaseCallback)
    }

    fun <T : UseCase.RequestValues, R : UseCase.ResponseValue> execute(useCase: UseCase<T, R>, values: T, callback: UseCase.UseCaseCallback<R>) {
        useCase.mRequestValues = values
        useCase.mUseCaseCallback = UiCallbackWrapper(callback, this)

        // The network request might be handled in a different thread so make sure
        // Espresso knows
        // that the app is busy until the response is handled.
        //EspressoIdlingResource.increment(); // App is busy until further notice

        mUseCaseScheduler.execute {
            useCase.run()
            // This callback may be called twice, once for the cache and once for loading
            // the data from the server API, so we check before decrementing, otherwise
            // it throws "Counter has been corrupted!" exception.
            //                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
            //                    EspressoIdlingResource.decrement(); // Set app as idle.
            //                }
        }
    }
}