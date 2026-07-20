/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class Event
object DeleteAllDataEventSuccess : Event()
object DeleteAllDataEventFailure : Event()
/**
 * @property pathDirectory
 */
data class ExportDataEventSuccess(val pathDirectory: String) : Event()
object ExportDataEventFailure : Event()

class SettingsViewModel : ViewModel(), KoinComponent {
    private val deleteAllDataUseCase: DeleteAllData by inject()
    private val exportDataUseCase: ExportData by inject()
    private val context: Context by inject()
    private val dispatcherProvider: DispatcherProvider by inject()
    private val _event: Subject<Event> = PublishSubject.create()

    fun deleteAllData() {
        viewModelScope.launch {
            deleteAllDataUseCase()
                .onSuccess {
                    delay(DELAY)
                    _event.onNext(DeleteAllDataEventSuccess)
                }
                .onFailure {
                    Timber.e(it)
                    _event.onNext(DeleteAllDataEventFailure)
                }
        }
    }

    /**
     * @return The directory name where the file is exported
     */
    fun exportData(dirUri: Uri) {
        viewModelScope.launch {
            exportDataUseCase()
                .mapCatching { withContext(dispatcherProvider.io()) { writeExportFile(it, dirUri) } }
                .onSuccess { _event.onNext(ExportDataEventSuccess(it)) }
                .onFailure {
                    Timber.e(it)
                    _event.onNext(ExportDataEventFailure)
                }
        }
    }

    private fun writeExportFile(exportBase: ExportBase, dirUri: Uri): String {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
        val dateTime = formatter.format(now)
        val json = Gson().toJson(exportBase)
        val dir = DocumentFile.fromTreeUri(context, dirUri)
        val filename = "$dateTime.elu"
        val file = dir?.createFile("application/octet-stream", filename)
            ?: throw Exception("Cannot open directory")
        val outputStream = context.contentResolver.openOutputStream(file.uri)
            ?: throw Exception("Cannot write in file")
        outputStream.use { stream ->
            stream.bufferedWriter().use { writer ->
                writer.write(json)
                writer.flush()
            }
        }
        return filename
    }

    fun observeEvent(): Subject<Event> {
        return _event
    }

    companion object {
        private const val DELAY = 1_000L
    }
}
