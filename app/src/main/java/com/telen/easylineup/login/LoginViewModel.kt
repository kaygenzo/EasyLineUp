/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ImportData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
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
    private val _loginEvent: Subject<LoginEvent> = PublishSubject.create()
    val disposables = CompositeDisposable()

    fun observeEvents(): Subject<LoginEvent> {
        return _loginEvent
    }

    fun importData(uri: Uri, updateIfExists: Boolean) {
        try {
            val task = context.contentResolver.openInputStream(uri)?.let {
                Completable.defer {
                    it.use { stream ->
                        stream.bufferedReader().use { reader ->
                            val data = Gson().fromJson(reader, ExportBase::class.java)
                            importDataUseCase(data, updateIfExists).ignoreElement()
                        }
                    }
                }
            } ?: let {
                Completable.error(IllegalArgumentException("Cannot open uri"))
            }

            val disposable = task.subscribe({
                _loginEvent.onNext(ImportSuccessfulEvent)
            }, {
                Timber.e(it)
                _loginEvent.onNext(ImportFailure)
            })
            disposables.add(disposable)
        } catch (e: Exception) {
            Timber.e(e)
            _loginEvent.onNext(ImportFailure)
        }
    }

    fun clear() {
        disposables.clear()
    }

    fun getMainTeam() {
        val disposable = getTeamUseCase()
            .subscribe({
                _loginEvent.onNext(GetTeamSuccess(it))
            }, {
                Timber.e(it)
                _loginEvent.onNext(GetTeamFailed)
            })
        disposables.add(disposable)
    }
}
