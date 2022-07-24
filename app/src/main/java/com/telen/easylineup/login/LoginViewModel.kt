package com.telen.easylineup.login

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.*

sealed class LoginEvent
object ImportSuccessfulEvent: LoginEvent()
object ImportFailure: LoginEvent()
data class GetTeamSuccess(val team: Team): LoginEvent()
object GetTeamFailed: LoginEvent()

class LoginViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    private val context: Context by inject()

    private val _loginEvent = PublishSubject.create<LoginEvent>()

    val disposables = CompositeDisposable()

    fun observeEvents(): Subject<LoginEvent> {
        return _loginEvent
    }

    fun importData(uri: Uri, updateIfExists: Boolean) {
        val task: Completable = context.contentResolver.openInputStream(uri)?.let {
            Completable.defer {
                it.use { stream ->
                    stream.bufferedReader().use { reader ->
                        val data = Gson().fromJson(reader, ExportBase::class.java)
                        domain.data().importData(data, updateIfExists)
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
    }

    fun clear() {
        disposables.clear()
    }

    fun getMainTeam() {
        val disposable = domain.teams().getTeam()
                .subscribe({
                    _loginEvent.onNext(GetTeamSuccess(it))
                }, {
                    Timber.e(it)
                    _loginEvent.onNext(GetTeamFailed)
                })
        disposables.add(disposable)
    }
}