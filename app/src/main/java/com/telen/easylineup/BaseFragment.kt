package com.telen.easylineup

import androidx.fragment.app.Fragment
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.Subject
import timber.log.Timber

abstract class BaseFragment(private val fragmentName: String) : Fragment() {
    val disposables by lazy { CompositeDisposable() }

    override fun onResume() {
        super.onResume()
        activity?.run {
            FirebaseAnalyticsUtils.onScreen(this, fragmentName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}

inline fun <reified T : Any> BaseFragment.launch(
    process: Flowable<T>,
    onNext: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onComplete: Action? = null,
    subscribeScheduler: Scheduler = Schedulers.computation(),
    observeScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    this.disposables.add(
        process
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(onNext,
                onError ?: Consumer {
                    Timber.e(it)
                },
                onComplete ?: Action { }
            )
    )
}

inline fun <reified T : Any> BaseFragment.launch(
    process: Maybe<T>,
    onSuccess: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onComplete: Action? = null,
    subscribeScheduler: Scheduler = Schedulers.computation(),
    observeScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    this.disposables.add(
        process
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(onSuccess,
                onError ?: Consumer {
                    Timber.e(it)
                },
                onComplete ?: Action { }
            )
    )
}

inline fun <reified T : Any> BaseFragment.launch(
    process: Single<T>,
    onSuccess: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    subscribeScheduler: Scheduler = Schedulers.computation(),
    observeScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    this.disposables.add(
        process
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(onSuccess, onError ?: Consumer {
                Timber.e(it)
            })
    )
}

fun BaseFragment.launch(
    process: Completable,
    onComplete: Action,
    onError: Consumer<in Throwable>? = null,
    subscribeScheduler: Scheduler = Schedulers.computation(),
    observeScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    this.disposables.add(
        process
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(onComplete, onError ?: Consumer {
                Timber.e(it)
            })
    )
}

inline fun <reified T : Any> BaseFragment.launch(
    process: Subject<T>,
    onSuccess: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    subscribeScheduler: Scheduler = Schedulers.computation(),
    observeScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    this.disposables.add(
        process
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(onSuccess, onError ?: Consumer {
                Timber.e(it)
            })
    )
}