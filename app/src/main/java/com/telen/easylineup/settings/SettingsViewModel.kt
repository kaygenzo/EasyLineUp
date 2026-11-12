/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.ThemeMode
import com.telen.easylineup.domain.model.export.DataCounts
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.toDataCounts
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.ExportFileWriter
import com.telen.easylineup.domain.ports.WrittenExportFile
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.GenerateExportFileName
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.utils.UserPreferencesStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @property folderName the picked folder's display name, when the SAF provider exposes one
 * @property fileName the written file's own name
 * @property counts what actually got written
 */
data class ExportDataEventSuccess(
    val folderName: String?,
    val fileName: String,
    val counts: DataCounts
)

/** One-shot actions a ViewModel can't do itself (navigate, start an Activity, log analytics). */
sealed class SettingsEvent
data object DeleteAllDataDone : SettingsEvent()

/** Deleting all data failed: it runs in one transaction, so nothing was deleted. */
data object DeleteAllDataFailed : SettingsEvent()
data object ExportSucceeded : SettingsEvent()
data object ExportFailed : SettingsEvent()
data object ImportSucceeded : SettingsEvent()
data object ImportFailed : SettingsEvent()

/** Everything [SettingsScreen] and its export/import sheets need, except the team info card. */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val menColor: Color = Color.Unspecified,
    val womenColor: Color = Color.Unspecified,
    val showDeleteAllDataSheet: Boolean = false,
    val exportPreview: ExportBase? = null,
    val exportSuccess: ExportDataEventSuccess? = null,
    val importFileName: String = "",
    val importPreview: ExportBase? = null,
    val importOverwriteExisting: Boolean = false,
    val importSuccess: ImportResult? = null
)

class SettingsViewModel(
    private val deleteAllDataUseCase: DeleteAllData,
    private val exportDataUseCase: ExportData,
    private val generateExportFileName: GenerateExportFileName,
    private val exportFileWriter: ExportFileWriter,
    private val preferences: UserPreferencesStore,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val stored = withContext(dispatcherProvider.io()) { preferences.read() }
            _uiState.update {
                it.copy(
                    themeMode = stored.themeMode,
                    menColor = Color(stored.menColor),
                    womenColor = Color(stored.womenColor),
                    importOverwriteExisting = stored.importOverwriteExisting
                )
            }
        }
    }

    fun onThemeSelected(mode: ThemeMode) {
        preferences.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun onMenColorPicked(color: Int) {
        preferences.setMenColor(color)
        _uiState.update { it.copy(menColor = Color(color)) }
    }

    fun onWomenColorPicked(color: Int) {
        preferences.setWomenColor(color)
        _uiState.update { it.copy(womenColor = Color(color)) }
    }

    fun onDeleteAllDataClick() {
        _uiState.update { it.copy(showDeleteAllDataSheet = true) }
    }

    fun onDeleteAllDataDismiss() {
        _uiState.update { it.copy(showDeleteAllDataSheet = false) }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            deleteAllDataUseCase()
                .onSuccess { _events.send(DeleteAllDataDone) }
                .onFailure {
                    Timber.e(it)
                    _events.send(DeleteAllDataFailed)
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
                .onSuccess { base -> _uiState.update { it.copy(exportPreview = base) } }
                .onFailure {
                    Timber.e(it)
                    _events.send(ExportFailed)
                }
        }
    }

    fun onExportPreviewDismiss() {
        _uiState.update { it.copy(exportPreview = null) }
    }

    fun onExportSuccessDismiss() {
        _uiState.update { it.copy(exportSuccess = null) }
    }

    /**
     * Writes the already-fetched export preview to the chosen folder - doesn't re-walk the
     * database, so what gets written matches exactly what the confirm sheet promised.
     */
    fun exportData(dirUri: Uri) {
        val exportBase = _uiState.value.exportPreview ?: return
        _uiState.update { it.copy(exportPreview = null) }
        viewModelScope.launch {
            runCatching {
                withContext(dispatcherProvider.io()) { writeExportFile(exportBase, dirUri) }
            }
                .onSuccess { written ->
                    _uiState.update {
                        it.copy(
                            exportSuccess = ExportDataEventSuccess(
                                written.folderName,
                                written.fileName,
                                exportBase.toDataCounts()
                            )
                        )
                    }
                    _events.send(ExportSucceeded)
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(ExportFailed)
                }
        }
    }

    private fun writeExportFile(exportBase: ExportBase, dirUri: Uri): WrittenExportFile =
        exportFileWriter.write(exportBase, dirUri.toString(), generateExportFileName())

    fun onImportPreviewReady(fileName: String, exportBase: ExportBase) {
        _uiState.update { it.copy(importFileName = fileName, importPreview = exportBase) }
    }

    fun onImportOverwriteChanged(checked: Boolean) {
        preferences.setImportOverwriteExisting(checked)
        _uiState.update { it.copy(importOverwriteExisting = checked) }
    }

    fun onImportPreviewDismiss() {
        _uiState.update { it.copy(importPreview = null) }
    }

    fun onImportSuccess(result: ImportResult) {
        _uiState.update { it.copy(importPreview = null, importSuccess = result) }
        viewModelScope.launch { _events.send(ImportSucceeded) }
    }

    fun onImportFailure() {
        viewModelScope.launch { _events.send(ImportFailed) }
    }

    fun onImportSuccessDismiss() {
        _uiState.update { it.copy(importSuccess = null) }
    }
}
