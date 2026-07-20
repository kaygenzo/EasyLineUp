/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ImportData
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

sealed class LoginEvent
object ImportSuccessfulEvent : LoginEvent()
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

    fun importData(uri: Uri, updateIfExists: Boolean) {
        viewModelScope.launch {
            runCatching {
                withContext(dispatcherProvider.io()) {
                    val stream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Cannot open uri")
                    val data = stream.use { s ->
                        s.bufferedReader().use { reader ->
                            Gson().fromJson(reader, ExportBase::class.java)
                        }
                    }
                    importDataUseCase(data, updateIfExists).getOrThrow()
                }
            }
                .onSuccess { _loginEvent.onNext(ImportSuccessfulEvent) }
                .onFailure {
                    Timber.e(it)
                    _loginEvent.onNext(ImportFailure)
                }
        }
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
