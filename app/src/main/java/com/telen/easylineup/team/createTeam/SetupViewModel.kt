package com.telen.easylineup.team.createTeam

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.*
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.TeamType
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.koin.core.KoinComponent
import org.koin.core.inject

class SetupViewModel: ViewModel(), KoinComponent {

    private val saveTeamUseCase: SaveTeam by inject()
    private val checkTeamUseCase: CheckTeam by inject()
    private val saveCurrentTeamUseCase: SaveCurrentTeam by inject()
    private val getTeamCreationNextStep: GetTeamCreationNextStep by inject()
    private val getTeamCreationPreviousStep: GetTeamCreationPreviousStep by inject()

    var team = Team(0, "", null, TeamType.BASEBALL.id, true)

    private var saveDisposable: Disposable? = null
    var stepLiveData: MutableLiveData<GetTeamCreationStep.ResponseValue> = MutableLiveData()
    var errorLiveData: MutableLiveData<Error> = MutableLiveData(Error.NONE)

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
        return UseCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team)).ignoreElement()
                .andThen(UseCaseHandler.execute(saveTeamUseCase, SaveTeam.RequestValues(team)).map { it.team })
                .flatMapCompletable { UseCaseHandler.execute(saveCurrentTeamUseCase, SaveCurrentTeam.RequestValues(it)).ignoreElement() }
                .doOnComplete { errorLiveData.value = Error.NONE }
    }

    fun getTeam(): Single<Team> {
        return Single.just(team)
    }

    fun nextButtonClicked(currentStep: Int) {
        dispose(saveDisposable)

        val requestValue = GetTeamCreationStep.RequestValues(
                TeamCreationStep.getStepById(currentStep) ?: TeamCreationStep.TEAM)

        saveDisposable = UseCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team)).ignoreElement()
                .doOnComplete { errorLiveData.value = Error.NONE }
                .andThen(UseCaseHandler.execute(getTeamCreationNextStep, requestValue))
                .subscribe({
                    stepLiveData.value = it
                }, {
                    if(it is NameEmptyException) {
                        errorLiveData.value = Error.NAME_EMPTY
                    }
                    else {
                        errorLiveData.value = Error.UNKNOWN
                    }
                })
    }

    fun previousButtonClicked(currentStep: Int) {
        dispose(saveDisposable)

        val requestValue = GetTeamCreationStep.RequestValues(
                TeamCreationStep.getStepById(currentStep) ?: TeamCreationStep.TEAM)

        saveDisposable =
                UseCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team)).ignoreElement()
                        .doOnComplete { errorLiveData.value = Error.NONE }
                        .onErrorResumeNext {
                            if(it is NameEmptyException && currentStep == TeamCreationStep.TEAM.id) {
                                Completable.complete()
                            }
                            else {
                                Completable.error(it)
                            }
                        }
                        .andThen(UseCaseHandler.execute(getTeamCreationPreviousStep, requestValue))
                        .subscribe({
                            stepLiveData.value = it
                        }, {
                            errorLiveData.value = Error.UNKNOWN
                        })
    }

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!it.isDisposed)
                it.dispose()
        }
    }

}