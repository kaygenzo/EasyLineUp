package com.telen.easylineup.team.createTeam

import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
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

   private val domain: ApplicationPort by inject()

    var team = Team(0, "", null, TeamType.BASEBALL.id, true)

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

    fun nextButtonClicked(currentStep: Int) {
        dispose(saveDisposable)
        saveDisposable = domain.getTeamCreationNextStep(currentStep, team)
                .subscribe({
                    errorLiveData.onNext(Error.NONE)
                    stepLiveData.onNext(it)
                }, {
                    if(it is NameEmptyException) {
                        errorLiveData.onNext(Error.NAME_EMPTY)
                    }
                    else {
                        errorLiveData.onNext(Error.UNKNOWN)
                    }
                })
    }

    fun previousButtonClicked(currentStep: Int) {
        dispose(saveDisposable)
        saveDisposable = domain.getTeamCreationPreviousStep(currentStep, team)
                        .subscribe({
                            errorLiveData.onNext(Error.NONE)
                            stepLiveData.onNext(it)
                        }, {
                            errorLiveData.onNext(Error.UNKNOWN)
                        })
    }

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!it.isDisposed)
                it.dispose()
        }
    }

}