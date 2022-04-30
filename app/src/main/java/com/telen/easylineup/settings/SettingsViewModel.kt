package com.telen.easylineup.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationInteractor
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

sealed class Event
object DeleteAllDataEventSuccess: Event()
object DeleteAllDataEventFailure: Event()
data class ExportDataEventSuccess(val pathDirectory: String): Event()
object ExportDataEventFailure: Event()

class SettingsViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()

    private val _event = PublishSubject.create<Event>()
    private val disposables = CompositeDisposable()

    fun clear() {
        disposables.clear()
    }

    fun deleteAllData() {
        val disposable = domain.deleteAllData()
                .andThen(Completable.timer(1000, TimeUnit.MILLISECONDS))
                .subscribe({
                    _event.onNext(DeleteAllDataEventSuccess)
                }, {
                    Timber.e(it)
                    _event.onNext(DeleteAllDataEventFailure)
                })
        disposables.add(disposable)
    }

    /**
     * @return The directory name where the file is exported
     */
    fun exportData(dirUri: Uri) {
        val disposable = domain.exportData(dirUri)
                .subscribe({
                    _event.onNext(ExportDataEventSuccess(it))
                }, {
                    Timber.e(it)
                    _event.onNext(ExportDataEventFailure)
                })
        disposables.add(disposable)
    }

    fun observeEvent(): Subject<Event> {
        return _event
    }
}