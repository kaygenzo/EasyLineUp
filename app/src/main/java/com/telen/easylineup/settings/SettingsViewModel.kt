package com.telen.easylineup.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class ExportDataObject(val fallbackName: String)

sealed class Event
object DeleteAllDataEventSuccess: Event()
object DeleteAllDataEventFailure: Event()
data class ExportDataEventSuccess(val pathDirectory: String): Event()
object ExportDataEventFailure: Event()

class SettingsViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val _event = PublishSubject.create<Event>()
    private val disposables = CompositeDisposable()

    private val _exportDataObjectLiveData = MutableLiveData<ExportDataObject>()
    val exportDataObjectLiveData: LiveData<ExportDataObject> = _exportDataObjectLiveData

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

    fun exportDataTriggered() {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
        val dateTime = formatter.format(now)
        _exportDataObjectLiveData.value = ExportDataObject(dateTime)
    }

    /**
     * @return The directory name where the file is exported
     */
    fun exportDataOnExternalMemory(name: String, fallbackName: String) {
        val disposable = domain.exportDataOnExternalMemory(name, fallbackName)
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