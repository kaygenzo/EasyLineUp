/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class Event
object DeleteAllDataEventSuccess : Event()
object DeleteAllDataEventFailure : Event()
/**
 * @property pathDirectory
 */
data class ExportDataEventSuccess(val pathDirectory: String) : Event()
object ExportDataEventFailure : Event()

class SettingsViewModel : ViewModel(), KoinComponent {
    private val useCaseHandler: UseCaseHandler by inject()
    private val deleteAllDataUseCase: DeleteAllData by inject()
    private val exportDataUseCase: ExportData by inject()
    private val context: Context by inject()
    private val _event: Subject<Event> = PublishSubject.create()
    private val disposables = CompositeDisposable()

    fun clear() {
        disposables.clear()
    }

    fun deleteAllData() {
        val disposable = useCaseHandler
            .execute(deleteAllDataUseCase, DeleteAllData.RequestValues())
            .ignoreElement()
            .andThen(Completable.timer(DELAY, TimeUnit.MILLISECONDS))
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
        val disposable = useCaseHandler
            .execute(exportDataUseCase, ExportData.RequestValues())
            .map { it.exportBase }
            .flatMap { writeExportFile(it, dirUri) }
            .subscribe({
                _event.onNext(ExportDataEventSuccess(it))
            }, {
                Timber.e(it)
                _event.onNext(ExportDataEventFailure)
            })
        disposables.add(disposable)
    }

    private fun writeExportFile(exportBase: ExportBase, dirUri: Uri): Single<String> {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
        val dateTime = formatter.format(now)
        val json = Gson().toJson(exportBase)
        val dir = DocumentFile.fromTreeUri(context, dirUri)
        val filename = "$dateTime.elu"
        val file = dir?.createFile("application/octet-stream", filename)
        return file?.let {
            context.contentResolver.openOutputStream(it.uri)?.let { os ->
                Single.defer {
                    os.use { stream ->
                        stream.bufferedWriter().use { writer ->
                            writer.write(json)
                            writer.flush()
                            Single.just(filename)
                        }
                    }
                }
            } ?: Single.error(Exception("Cannot write in file"))
        } ?: Single.error(Exception("Cannot open directory"))
    }

    fun observeEvent(): Subject<Event> {
        return _event
    }

    companion object {
        private const val DELAY = 1_000L
    }
}
