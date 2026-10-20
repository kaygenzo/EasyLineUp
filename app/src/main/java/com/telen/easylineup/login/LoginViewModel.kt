/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.export.json.JsonExportBase
import com.telen.easylineup.export.json.exportGson
import com.telen.easylineup.export.json.toDomain
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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

class LoginViewModel : ViewModel(), KoinComponent {
    private val getTeamUseCase: GetTeam by inject()
    private val importDataUseCase: ImportData by inject()
    private val context: Context by inject()
    private val dispatcherProvider: DispatcherProvider by inject()
    private val _loginEvent: Subject<LoginEvent> = PublishSubject.create()

    fun observeEvents(): Subject<LoginEvent> {
        return _loginEvent
    }

    /**
     * Parses the picked file into an [ExportBase] preview - no database writes yet, so the
     * confirm sheet can show real counts before [confirmImport] actually applies them.
     */
    fun peekImportFile(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(dispatcherProvider.io()) {
                    val stream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Cannot open uri")
                    stream.use { s ->
                        s.bufferedReader().use { reader ->
                            exportGson.fromJson(reader, JsonExportBase::class.java).toDomain()
                        }
                    }
                }
            }
                .onSuccess {
                    _loginEvent.onNext(ImportPreviewReady(queryDisplayName(uri), it))
                }
                .onFailure {
                    Timber.e(it)
                    _loginEvent.onNext(ImportFailure)
                }
        }
    }

    fun confirmImport(exportBase: ExportBase, updateIfExists: Boolean) {
        viewModelScope.launch {
            importDataUseCase(exportBase, updateIfExists)
                .onSuccess { _loginEvent.onNext(ImportSuccessfulEvent(it)) }
                .onFailure {
                    Timber.e(it)
                    _loginEvent.onNext(ImportFailure)
                }
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return uri.lastPathSegment.orEmpty()
    }

    fun clear() {
    }

    fun getMainTeam() {
        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess { _loginEvent.onNext(GetTeamSuccess(it)) }
                .onFailure {
                    Timber.e(it)
                    _loginEvent.onNext(GetTeamFailed)
                }
        }
    }
}
