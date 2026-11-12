/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.ImportFileReader
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.utils.UserPreferencesStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

sealed class LoginEvent
/**
 * @property fileName the picked file's display name, for the confirm/success sheets
 * @property exportBase the parsed (not yet applied) payload
 */
data class ImportPreviewReady(val fileName: String, val exportBase: ExportBase) : LoginEvent()
/**
 * @property result per-entity inserted/updated counts, for the success sheet
 */
data class ImportSuccessfulEvent(val result: ImportResult) : LoginEvent()
object ImportFailure : LoginEvent()
/**
 * @property team
 */
data class GetTeamSuccess(val team: Team) : LoginEvent()
object GetTeamFailed : LoginEvent()

class LoginViewModel(
    private val getTeamUseCase: GetTeam,
    private val importDataUseCase: ImportData,
    private val importFileReader: ImportFileReader,
    private val dispatcherProvider: DispatcherProvider,
    private val preferences: UserPreferencesStore
) : ViewModel() {
    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    private val _importOverwriteExisting = MutableStateFlow(false)

    /** The import sheet's "update what already exists" switch, as last left by the user. */
    val importOverwriteExisting: StateFlow<Boolean> = _importOverwriteExisting.asStateFlow()

    init {
        viewModelScope.launch {
            _importOverwriteExisting.value = withContext(dispatcherProvider.io()) {
                preferences.read().importOverwriteExisting
            }
        }
    }

    fun onImportOverwriteChanged(checked: Boolean) {
        preferences.setImportOverwriteExisting(checked)
        _importOverwriteExisting.value = checked
    }

    /**
     * Parses the picked file into an [ExportBase] preview - no database writes yet, so the
     * confirm sheet can show real counts before [confirmImport] actually applies them.
     */
    fun peekImportFile(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(dispatcherProvider.io()) { importFileReader.read(uri.toString()) }
            }
                .onSuccess {
                    val displayName = importFileReader.queryDisplayName(uri.toString())
                    _events.send(ImportPreviewReady(displayName, it))
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(ImportFailure)
                }
        }
    }

    fun confirmImport(exportBase: ExportBase, updateIfExists: Boolean) {
        viewModelScope.launch {
            importDataUseCase(exportBase, updateIfExists)
                .onSuccess { _events.send(ImportSuccessfulEvent(it)) }
                .onFailure {
                    Timber.e(it)
                    _events.send(ImportFailure)
                }
        }
    }

    fun getMainTeam() {
        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess { _events.send(GetTeamSuccess(it)) }
                .onFailure {
                    Timber.e(it)
                    _events.send(GetTeamFailed)
                }
        }
    }
}
