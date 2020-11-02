package com.telen.easylineup.login

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.domain.application.ApplicationPort
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

    private val domain: ApplicationPort by inject()

    private val _loginEvent = PublishSubject.create<LoginEvent>()

    val disposables = CompositeDisposable()

    fun observeEvents(): Subject<LoginEvent> {
        return _loginEvent
    }

    fun importData(dataPath: String, updateIfExists: Boolean) {
        try {
            val inputStream: InputStream = FileInputStream(File(dataPath))
            importData(inputStream, updateIfExists)
        }
        catch (e: Exception) {
            _loginEvent.onNext(ImportFailure)
        }
    }

    fun importData(inputStream: InputStream, updateIfExists: Boolean) {
        var input : BufferedReader? = null
        val task = try {
            input = BufferedReader(InputStreamReader(inputStream))
            val data = Gson().fromJson(input, ExportBase::class.java)
            domain.importData(data, updateIfExists)
        }
        catch (e: Exception) {
            Completable.error(e)
        }
        finally {
            input?.close()
            inputStream.close()
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
        val disposable = domain.getTeam()
                .subscribe({
                    _loginEvent.onNext(GetTeamSuccess(it))
                }, {
                    Timber.e(it)
                    _loginEvent.onNext(GetTeamFailed)
                })
        disposables.add(disposable)
    }
}