package com.telen.easylineup.team.createTeam

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetTeamCreationNextStep
import com.telen.easylineup.domain.NameEmptyException
import com.telen.easylineup.domain.SaveCurrentTeam
import com.telen.easylineup.domain.SaveTeam
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.TeamType
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class SetupViewModel: ViewModel() {

    private val saveTeamUseCase = SaveTeam(App.database.teamDao())
    private val saveCurrentTeamUseCase = SaveCurrentTeam(App.database.teamDao())
    private val getTeamCreationNextStep = GetTeamCreationNextStep()

    var team = Team(0, "", null, TeamType.BASEBALL.id, true)

    private var saveDisposable: Disposable? = null
    var stepLiveData: MutableLiveData<GetTeamCreationNextStep.ResponseValue> = MutableLiveData()
    var errorLiveData: MutableLiveData<Error> = MutableLiveData()

    enum class Error {
        NAME_EMPTY,
        UNKNOWN
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
        return UseCaseHandler.execute(saveTeamUseCase, SaveTeam.RequestValues(team)).map { it.team }
                .flatMapCompletable { UseCaseHandler.execute(saveCurrentTeamUseCase, SaveCurrentTeam.RequestValues(it)).ignoreElement() }

    }

    fun getTeam(): Single<Team> {
        return Single.just(team)
    }

    fun nextButtonClicked(currentStep: Int) {
        dispose(saveDisposable)

        val requestValue = GetTeamCreationNextStep.RequestValues(
                GetTeamCreationNextStep.TeamCreationStep.getStepById(currentStep) ?: GetTeamCreationNextStep.TeamCreationStep.TEAM)

        saveDisposable =
                saveTeam()
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

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!it.isDisposed)
                it.dispose()
        }
    }

    fun backPressed() {

    }
}