/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

private class RxLiveData<T : Any>(private val flowable: Flowable<T>) : LiveData<T>() {
    private var disposable: Disposable? = null

    override fun onActive() {
        disposable = flowable.subscribe({ postValue(it) }, { Timber.e(it) })
    }

    override fun onInactive() {
        disposable?.dispose()
        disposable = null
    }
}

fun <T : Any> Flowable<T>.toLiveData(): LiveData<T> = RxLiveData(this)
