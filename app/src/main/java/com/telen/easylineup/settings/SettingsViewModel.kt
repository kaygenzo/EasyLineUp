/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.compose.DataCounts
import com.telen.easylineup.compose.toDataCounts
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.export.json.exportGson
import com.telen.easylineup.export.json.toJson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class Event
object DeleteAllDataEventSuccess : Event()
object DeleteAllDataEventFailure : Event()
/**
 * @property exportBase the data about to be exported, before the folder picker runs
 */
data class ExportPreviewReady(val exportBase: ExportBase) : Event()
/**
 * @property folderName the picked folder's display name, when the SAF provider exposes one
 * @property fileName the written file's own name
 * @property counts what actually got written
 */
data class ExportDataEventSuccess(
    val folderName: String?,
    val fileName: String,
    val counts: DataCounts
) : Event()
object ExportDataEventFailure : Event()
/**
 * @property count
 */
data class GetTeamsCountSuccess(val count: Int) : Event()
object GetTeamsCountFailure : Event()

class SettingsViewModel(
    private val deleteAllDataUseCase: DeleteAllData,
    private val exportDataUseCase: ExportData,
    private val getAllTeamsUseCase: GetAllTeams,
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    /**
     * Used by the Team row to know whether Team Details should show the
     * edit-only or edit-or-delete menu, same rule as the drawer header today.
     */
    fun getTeamsCount() {
        viewModelScope.launch {
            getAllTeamsUseCase()
                .onSuccess { _events.send(GetTeamsCountSuccess(it.size)) }
                .onFailure {
                    Timber.e(it)
                    _events.send(GetTeamsCountFailure)
                }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            deleteAllDataUseCase()
                .onSuccess {
                    delay(DELAY)
                    _events.send(DeleteAllDataEventSuccess)
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(DeleteAllDataEventFailure)
                }
        }
    }

    /**
     * Walks the database to build the export payload, without writing anything yet - lets the
     * confirm sheet show real counts before the native folder picker opens.
     */
    fun loadExportPreview() {
        viewModelScope.launch {
            exportDataUseCase()
                .onSuccess { _events.send(ExportPreviewReady(it)) }
                .onFailure {
                    Timber.e(it)
                    _events.send(ExportDataEventFailure)
                }
        }
    }

    /**
     * Writes the already-fetched [exportBase] (from [loadExportPreview]) to the chosen folder -
     * doesn't re-walk the database, so what gets written matches exactly what the confirm sheet
     * promised.
     */
    fun exportData(exportBase: ExportBase, dirUri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(dispatcherProvider.io()) { writeExportFile(exportBase, dirUri) }
            }
                .onSuccess { (fileName, folderName) ->
                    _events.send(
                        ExportDataEventSuccess(folderName, fileName, exportBase.toDataCounts())
                    )
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(ExportDataEventFailure)
                }
        }
    }

    private fun writeExportFile(exportBase: ExportBase, dirUri: Uri): Pair<String, String?> {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
        val dateTime = formatter.format(now)
        val json = exportGson.toJson(exportBase.toJson())
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
        return filename to dir.name
    }

    companion object {
        private const val DELAY = 1_000L
    }
}
