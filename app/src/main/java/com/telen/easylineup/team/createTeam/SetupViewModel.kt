package com.telen.easylineup.team.createTeam

import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.StepConfiguration
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject

class SetupViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    var currentStep: Int = 0
    var lastConfiguration: StepConfiguration? = null

    var team = Team(0, "", null, TeamType.UNKNOWN.id, true)

    private var saveDisposable: Disposable? = null
    var stepLiveData = PublishSubject.create<StepConfiguration>()
    var errorLiveData = PublishSubject.create<Error>()

    enum class Error {
        NAME_EMPTY,
        UNKNOWN,
        NONE
    }

    fun setTeamName(name: String) {
        team.name = name
    }

    fun setTeamImage(image: String?) {
        team.image = image
    }

    fun setTeamType(position: Int) {
        TeamType.values().firstOrNull { it.position == position }?.let {
            team.type = it.id
        }
    }

    fun saveTeam(): Completable {
        return domain.saveTeam(team).doOnComplete {
            errorLiveData.onNext(Error.NONE)
        }
    }

    fun getTeam(): Single<Team> {
        return Single.just(team)
    }

    fun nextButtonClicked() {
        dispose(saveDisposable)
        saveDisposable = domain.getTeamCreationNextStep(currentStep, team)
                .subscribe({
                    errorLiveData.onNext(Error.NONE)
                    stepLiveData.onNext(it)
                    lastConfiguration = it
                }, {
                    if(it is NameEmptyException) {
                        errorLiveData.onNext(Error.NAME_EMPTY)
                    }
                    else {
                        errorLiveData.onNext(Error.UNKNOWN)
                    }
                })
    }

    fun previousButtonClicked() {
        dispose(saveDisposable)
        saveDisposable = domain.getTeamCreationPreviousStep(currentStep, team)
                .subscribe({
                    errorLiveData.onNext(Error.NONE)
                    stepLiveData.onNext(it)
                    lastConfiguration = it
                }, {
                    errorLiveData.onNext(Error.UNKNOWN)
                })
    }

    fun refresh() {
        lastConfiguration?.run {
            stepLiveData.onNext(this)
        }
    }

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!it.isDisposed)
                it.dispose()
        }
    }

}