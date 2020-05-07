package com.telen.easylineup.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.utils.SingleLiveEvent
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

sealed class LoginEvent
object ImportSuccessfulEvent: LoginEvent()
object ImportFailure: LoginEvent()
data class GetTeamSuccess(val team: Team): LoginEvent()
object GetTeamFailed: LoginEvent()

class LoginViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val _loginEvent = SingleLiveEvent<LoginEvent>()
    val loginEvent: LiveData<LoginEvent> = _loginEvent

    val disposables = CompositeDisposable()

    fun importData(dataPath: String, updateIfExists: Boolean) {
        var input : BufferedReader? = null
        val task = try {
            input = BufferedReader(FileReader(File(dataPath)))
            val data = Gson().fromJson(input, ExportBase::class.java)
            domain.importData(data, updateIfExists)
        }
        catch (e: Exception) {
            Completable.error(e)
        }
        finally {
            input?.close()
        }

        val disposable = task.subscribe({
            _loginEvent.postValue(ImportSuccessfulEvent)
        }, {
            Timber.e(it)
            _loginEvent.postValue(ImportFailure)
        })
        disposables.add(disposable)
    }

    fun clear() {
        disposables.clear()
    }

    fun getMainTeam() {
        val disposable = domain.getTeam()
                .subscribe({
                    _loginEvent.postValue(GetTeamSuccess(it))
                }, {
                    Timber.e(it)
                    _loginEvent.postValue(GetTeamFailed)
                })
        disposables.add(disposable)
    }
}